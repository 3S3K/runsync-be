#!/usr/bin/env node
/**
 * RunSync 동시 사용자 부하 테스트. 외부 패키지 없이 Node 22 이상에서 실행한다.
 *
 * 가상 사용자마다: WebSocket 접속 → 러닝룸(협동 러닝) 구독 → 러닝 시작 → GPS 송수신 → 러닝 종료 → 기록 저장
 *
 *   # 가상 계정 모드: 계정마다 토큰을 직접 만든다 (서버와 같은 JWT_SECRET 필요, 로컬 환경용)
 *   JWT_SECRET=... node load-test.mjs --target http://localhost:8090 --users 50 --first-user-id <first_user_id> --room <room_id>
 *
 *   # 단일 계정 모드: 한 계정의 토큰으로 연결 수만 늘린다 (러닝 세션·러닝룸 없이 접속과 송수신만)
 *   ACCESS_TOKEN=... node load-test.mjs --target https://api.example.com --users 50
 */
import { execFileSync } from 'node:child_process';
import { createHmac } from 'node:crypto';
import { setTimeout as sleep } from 'node:timers/promises';

// "--이름 값" 쌍만 받는다. 값이 빠진 옵션은 기본값으로 넘어가지 않도록 따로 모아 오류로 알린다
const args = {};
const missingValues = [];
process.argv.slice(2).forEach((value, index, all) => {
  if (!value.startsWith('--')) {
    return;
  }
  const next = all[index + 1];
  if (next === undefined || next.startsWith('--')) {
    missingValues.push(value);
  } else {
    args[value.slice(2)] = next;
  }
});
if (missingValues.length) {
  console.error(`값이 빠진 옵션이 있습니다: ${missingValues.join(', ')}`);
  process.exit(1);
}
const TARGET = (args.target || 'http://localhost:8090').replace(/\/$/, '');
const USERS = Number(args.users || 5);
const DURATION_SEC = Number(args.duration || 30);
const INTERVAL_MS = Number(args.interval || 1000);        // GPS 전송 주기
const FIRST_USER_ID = Number(args['first-user-id'] || 1);
const ROOM_ID = args.room !== undefined ? Number(args.room) : null;      // 협동 러닝(러닝룸) 번호
const SERVER_PID = args['server-pid'] || null;             // 주면 서버 프로세스의 CPU·메모리를 함께 잰다
const JWT_SECRET = process.env.JWT_SECRET || '';
const SHARED_TOKEN = process.env.ACCESS_TOKEN || '';
const IS_SHARED_ACCOUNT = !JWT_SECRET;

const CONNECT_TIMEOUT_MS = 10000;
const REQUEST_TIMEOUT_MS = 10000;      // REST 요청 하나의 제한 시간. 넘기면 실패로 센다
const PING_TIMEOUT_MS = 5000;          // 이 시간 안에 응답이 없는 핑은 시간 초과로 세고, 그 연결의 핑 측정을 멈춘다
const SAVE_EVERY_SEC = 5;              // 실제 앱이 누적 거리·시간을 저장하는 주기
const SAMPLE_EVERY_MS = 2000;          // 서버 CPU·메모리를 재는 주기
const SETTLE_MS = 500;
const LAST_MESSAGE_WAIT_MS = 1500;

if (IS_SHARED_ACCOUNT && !SHARED_TOKEN) {
  console.error('JWT_SECRET(가상 계정 모드) 또는 ACCESS_TOKEN(단일 계정 모드) 환경변수가 필요합니다.');
  process.exit(1);
}
const invalid = [
  ['users', Number.isInteger(USERS) && USERS > 0],
  ['duration', Number.isFinite(DURATION_SEC) && DURATION_SEC > 0],
  ['interval', Number.isFinite(INTERVAL_MS) && INTERVAL_MS > 0],
  ['first-user-id', Number.isInteger(FIRST_USER_ID) && FIRST_USER_ID > 0],
  ['room', ROOM_ID == null || (Number.isInteger(ROOM_ID) && ROOM_ID > 0)],
].filter(([, ok]) => !ok).map(([name]) => `--${name}`);
if (invalid.length) {
  console.error(`값이 올바르지 않습니다: ${invalid.join(', ')}`);
  process.exit(1);
}

const WS_URL = `${TARGET.replace(/^http/, 'ws')}/ws/locations`;
const BASE = { lat: 36.3321, lng: 127.4562 };

// ----- 토큰 -----

function base64url(value) {
  return Buffer.from(value).toString('base64url');
}

/** 서버(jjwt)와 같은 방식: 키 길이에 맞는 HMAC 알고리즘으로 서명한다 */
function mintToken(userId) {
  const bits = Buffer.byteLength(JWT_SECRET) * 8;
  const [alg, hash] = bits >= 512 ? ['HS512', 'sha512'] : bits >= 384 ? ['HS384', 'sha384'] : ['HS256', 'sha256'];
  const now = Math.floor(Date.now() / 1000);
  const body = `${base64url(JSON.stringify({ alg }))}.${base64url(JSON.stringify({
    sub: String(userId), role: 'USER', iat: now, exp: now + 3600,
  }))}`;
  return `${body}.${createHmac(hash, JWT_SECRET).update(body).digest('base64url')}`;
}

// ----- 최소한의 STOMP 클라이언트 -----

class StompClient {
  constructor(token) {
    this.token = token;
    this.handlers = new Map();
    this.errors = [];
    this.closed = false;
  }

  connect() {
    return new Promise((resolve, reject) => {
      let settled = false;
      // 성공이든 실패든 한 번만 확정한다. 실패로 확정하면 소켓을 닫아, 뒤늦게 붙은 연결이 서버에 남지 않게 한다
      const settle = (error) => {
        if (settled) {
          return;
        }
        settled = true;
        clearTimeout(timer);
        if (error) {
          this.disconnect();
          reject(error);
        } else {
          resolve();
        }
      };
      const timer = setTimeout(() => settle(new Error('연결 시간 초과')), CONNECT_TIMEOUT_MS);

      this.socket = new WebSocket(WS_URL);
      this.socket.onopen = () => this.frame('CONNECT', {
        'accept-version': '1.2', 'heart-beat': '0,0', Authorization: `Bearer ${this.token}`,
      });
      this.socket.onerror = () => settle(new Error('소켓 오류'));
      this.socket.onclose = () => {
        this.closed = true;
        settle(new Error('연결 종료'));
      };
      this.socket.onmessage = (event) => {
        String(event.data).split('\0').forEach((raw) => {
          const text = raw.replace(/^\n+/, '');
          if (!text) {
            return;
          }
          const [head, ...rest] = text.split('\n\n');
          const [command, ...headerLines] = head.split('\n');
          const headers = Object.fromEntries(headerLines.map((line) => {
            const at = line.indexOf(':');
            return [line.slice(0, at), line.slice(at + 1)];
          }));
          const payload = rest.join('\n\n');
          if (command === 'CONNECTED') {
            settle();
          } else if (command === 'ERROR') {
            this.errors.push(headers.message || payload);
            settle(new Error(headers.message || 'STOMP 오류'));
          } else if (command === 'MESSAGE') {
            try {
              this.handlers.get(headers.subscription)?.(payload);
            } catch (error) {
              this.errors.push(`메시지 처리 실패: ${error.message}`);
            }
          }
        });
      };
    });
  }

  frame(command, headers, body = '') {
    const head = Object.entries(headers).map(([key, value]) => `${key}:${value}`).join('\n');
    this.socket.send(`${command}\n${head}\n\n${body}\0`);
  }

  subscribe(destination, id, handler) {
    this.handlers.set(id, handler);
    this.frame('SUBSCRIBE', { id, destination });
  }

  send(destination, body) {
    this.frame('SEND', { destination, 'content-type': 'application/json' }, JSON.stringify(body));
  }

  disconnect() {
    this.closed = true;
    this.handlers.clear();
    if (!this.socket) {
      return;
    }
    this.socket.onopen = null;
    this.socket.onmessage = null;
    this.socket.onerror = null;
    this.socket.onclose = null;
    try {
      this.socket.close();
    } catch {
      // 이미 끊긴 연결
    }
  }
}

// ----- REST -----

async function api(token, method, path, body) {
  const response = await fetch(`${TARGET}${path}`, {
    method,
    headers: { 'Content-Type': 'application/json', Authorization: `Bearer ${token}` },
    body: body ? JSON.stringify(body) : undefined,
    signal: AbortSignal.timeout(REQUEST_TIMEOUT_MS),
  });
  const json = await response.json().catch(() => null);
  if (!response.ok) {
    throw new Error(`${method} ${path} → ${response.status} ${json?.message ?? ''}`);
  }
  return json?.data ?? null;
}

// ----- 측정값 -----

const stats = {
  connectOk: 0, connectFail: 0,
  startOk: 0, startFail: 0,
  sent: 0, received: 0, latencies: [],
  pingSent: 0, pingTimeout: 0,
  locationSaveOk: 0, locationSaveFail: 0,
  endOk: 0, endFail: 0,
  recordOk: 0, recordFail: 0,
  unexpected: 0,
  errors: [],
};
const sentAt = new Map();      // "보낸 사용자:위도" → 보낸 시각. 받은 쪽에서 지연 시간을 계산한다
const serverSamples = [];      // { at, cpuSec, memoryMb }

function fail(kind, error) {
  stats[kind] += 1;
  if (stats.errors.length < 10) {
    stats.errors.push(error.message);
  }
}

/** ps 의 누적 CPU 시간([DD-][HH:]MM:SS[.ss])을 초로 바꾼다. macOS 와 Linux 의 표기를 모두 받는다 */
function parseCpuSeconds(text) {
  const [days, clock] = text.includes('-') ? text.split('-') : ['0', text];
  return Number(days) * 86400 + clock.split(':').reduce((total, part) => total * 60 + Number(part), 0);
}

function sampleServer() {
  if (!SERVER_PID) {
    return;
  }
  try {
    // %cpu 는 운영체제마다 기준 구간이 달라(Linux 는 프로세스 실행 시간 전체의 평균) 쓰지 않고, 누적 CPU 시간을 읽어 차이로 계산한다
    const [cpuTime, rssKb] = execFileSync('ps', ['-o', 'time=,rss=', '-p', SERVER_PID]).toString().trim().split(/\s+/);
    serverSamples.push({ at: performance.now(), cpuSec: parseCpuSeconds(cpuTime), memoryMb: Number(rssKb) / 1024 });
  } catch {
    // 프로세스를 찾지 못하면 건너뛴다
  }
}

/** 표본 사이에 늘어난 CPU 시간을 지난 시간으로 나눈 사용률(코어 하나 = 100%). Linux ps 는 초 단위라 구간별 최댓값은 거칠다 */
function cpuUsage(samples) {
  if (samples.length < 2) {
    return { avg: null, max: null };
  }
  const usage = (from, to) => ((to.cpuSec - from.cpuSec) / ((to.at - from.at) / 1000)) * 100;
  return {
    avg: usage(samples[0], samples[samples.length - 1]),
    max: Math.max(...samples.slice(1).map((sample, index) => usage(samples[index], sample))),
  };
}

// ----- 가상 사용자 한 명 -----

/**
 * @param {number} index 가상 사용자 순번
 * @param {Promise<void>} startSignal 준비가 끝난 사용자 전원이 동시에 달리기 시작하는 신호
 * @param {(isReady: boolean) => void} reportReady 접속·구독·러닝 시작이 끝났는지(또는 접속에 실패했는지) 알린다
 */
async function runUser(index, startSignal, reportReady) {
  const userId = FIRST_USER_ID + index;
  const token = IS_SHARED_ACCOUNT ? SHARED_TOKEN : mintToken(userId);
  const client = new StompClient(token);

  try {
    await client.connect();
    stats.connectOk += 1;
  } catch (error) {
    fail('connectFail', error);
    reportReady(false);
    return;
  }

  // 러닝룸이 없으면 서버의 핑 응답으로 왕복 시간을 잰다.
  // 핑 응답은 같은 계정의 모든 연결로 전달되고 요청 식별자가 없으므로, 단일 계정 모드에서는 연결 하나만 핑을 보낸다.
  const probe = ROOM_ID == null && (!IS_SHARED_ACCOUNT || index === 0) ? { sentAt: null, stopped: false } : null;

  if (ROOM_ID != null) {
    client.subscribe(`/topic/artrun/${ROOM_ID}`, 'room', (payload) => {
      const message = JSON.parse(payload);
      if (message.type !== 'ARTRUN_LOCATION_UPDATE') {
        return;
      }
      const sent = sentAt.get(`${message.data.userId}:${message.data.latitude.toFixed(6)}`);
      if (sent) {
        stats.received += 1;
        stats.latencies.push(performance.now() - sent);
      }
    });
  } else if (probe) {
    client.subscribe('/user/queue/ping', 'ping', () => {
      // 시간 초과가 난 뒤에는 응답이 어느 핑의 것인지 구분할 수 없으므로 세지 않는다
      if (!probe.stopped && probe.sentAt != null) {
        stats.received += 1;
        stats.latencies.push(performance.now() - probe.sentAt);
        probe.sentAt = null;
      }
    });
  }

  let sessionId = null;
  if (!IS_SHARED_ACCOUNT) {
    const startBody = { startTime: new Date(Date.now() - 1000).toISOString(), ...(ROOM_ID != null ? { artRunSessionId: ROOM_ID } : null) };
    try {
      sessionId = (await api(token, 'POST', '/api/run-sessions', startBody)).sessionId;
      stats.startOk += 1;
    } catch (error) {
      fail('startFail', error);
    }
  }

  reportReady(true);
  await startSignal;

  const ticks = Math.round((DURATION_SEC * 1000) / INTERVAL_MS);
  const saves = [];
  let savedCount = 0;
  for (let tick = 0; tick < ticks && !client.closed; tick += 1) {
    const elapsedSec = (tick * INTERVAL_MS) / 1000;
    const latitude = Number((BASE.lat + index * 0.001 + tick * 0.00001).toFixed(6));
    const longitude = Number((BASE.lng + index * 0.001).toFixed(6));

    if (ROOM_ID != null) {
      sentAt.set(`${userId}:${latitude.toFixed(6)}`, performance.now());
    } else if (probe) {
      // 응답을 기다리는 핑이 있으면 새로 보내지 않는다. 보낸 시각이 덮어써지면 지연이 실제보다 짧게 잡힌다
      if (probe.sentAt != null && performance.now() - probe.sentAt > PING_TIMEOUT_MS) {
        stats.pingTimeout += 1;
        probe.stopped = true;
        probe.sentAt = null;
      }
      if (!probe.stopped && probe.sentAt == null) {
        probe.sentAt = performance.now();
        client.send('/app/ping', {});
        stats.pingSent += 1;
      }
    }
    client.send('/app/location', {
      type: 'LOCATION_UPDATE',
      data: { ...(sessionId ? { sessionId } : null), ...(ROOM_ID != null ? { artRunSessionId: ROOM_ID } : null), latitude, longitude },
    });
    stats.sent += 1;

    // 실제 앱처럼 5초마다 누적 거리·시간을 저장한다. 전송 주기와 무관하게 지난 시간으로 판단한다
    const saveTurn = Math.floor(elapsedSec / SAVE_EVERY_SEC);
    if (sessionId && saveTurn > savedCount) {
      savedCount = saveTurn;
      saves.push(api(token, 'PATCH', `/api/run-sessions/${sessionId}/location`, {
        lastLatitude: latitude,
        lastLongitude: longitude,
        currentDistance: Number((elapsedSec * 0.003).toFixed(2)),
        currentDurationTime: Math.round(elapsedSec),
      }).then(() => { stats.locationSaveOk += 1; }, (error) => fail('locationSaveFail', error)));
    }
    await sleep(INTERVAL_MS);
  }

  // 마지막 메시지가 도착할 시간을 주고, 보낸 저장 요청의 결과가 모두 확정된 뒤에 러닝을 종료한다
  await Promise.all([sleep(LAST_MESSAGE_WAIT_MS), ...saves]);
  if (probe?.sentAt != null) {
    // 마지막 핑도 제한 시간을 다 채워 기다린 뒤에 판정한다. 기다리는 동안 온 응답은 수신으로 잡힌다
    const remainingMs = PING_TIMEOUT_MS - (performance.now() - probe.sentAt);
    if (remainingMs > 0 && !client.closed) {
      await sleep(remainingMs);
    }
    if (probe.sentAt != null) {
      stats.pingTimeout += 1;
      probe.stopped = true;
      probe.sentAt = null;
    }
  }

  if (sessionId) {
    try {
      await api(token, 'PATCH', `/api/run-sessions/${sessionId}`, {
        endTime: new Date().toISOString(), totalDistance: Number((DURATION_SEC * 0.003).toFixed(2)),
      });
      stats.endOk += 1;
      try {
        await api(token, 'PATCH', `/api/run-sessions/${sessionId}/records`, {
          averagePace: 6.2, calories: 30, averageHeartRate: 150, cadence: 165, elevationGain: 2,
        });
        stats.recordOk += 1;
      } catch (error) {
        fail('recordFail', error);
      }
    } catch (error) {
      fail('endFail', error);
    }
  }
  stats.errors.push(...client.errors.slice(0, 2));
  client.disconnect();
}

// ----- 실행 -----

function percentile(sorted, ratio) {
  return sorted.length ? sorted[Math.min(sorted.length - 1, Math.floor(sorted.length * ratio))] : null;
}

function rate(ok, total) {
  return total ? Number(((ok / total) * 100).toFixed(1)) : null;
}

const start = Promise.withResolvers();
const users = [];
const readiness = [];
for (let index = 0; index < USERS; index += 1) {
  const ready = Promise.withResolvers();
  readiness.push(ready.promise);
  users.push(runUser(index, start.promise, ready.resolve).catch((error) => {
    fail('unexpected', error);
    ready.resolve(false);
  }));
  await sleep(20);      // 접속이 한 순간에 몰리지 않게 조금씩 띄운다
}

// 모든 사용자의 준비 결과가 확정된 뒤 참가자를 고정한다. 시작 신호를 받는 집합과 전달률의 분모가 같아진다
const participants = (await Promise.all(readiness)).filter(Boolean).length;
if (participants === 0) {
  console.error('접속에 성공한 사용자가 없습니다.', [...new Set(stats.errors)].slice(0, 5));
  process.exit(1);
}
await sleep(SETTLE_MS);      // 단순 브로커는 구독 확인(RECEIPT)을 주지 않으므로, 구독 프레임이 처리될 시간을 조금 둔다

sampleServer();
const sampler = setInterval(sampleServer, SAMPLE_EVERY_MS);
start.resolve();
await Promise.all(users);
clearInterval(sampler);
sampleServer();

const sorted = [...stats.latencies].sort((a, b) => a - b);
// 러닝룸에서는 보낸 위치 하나를 참가자 모두가 받는다. 핑은 보낸 연결이 응답 하나를 받는다
const expected = ROOM_ID != null ? stats.sent * participants : stats.pingSent;
const round = (value) => (value == null ? null : Number(value.toFixed(1)));
const cpu = cpuUsage(serverSamples);

const result = {
  target: TARGET,
  mode: IS_SHARED_ACCOUNT ? '단일 계정' : '가상 계정',
  users: USERS,
  participants,
  durationSec: DURATION_SEC,
  connectSuccessPct: rate(stats.connectOk, USERS),
  runStartSuccessPct: IS_SHARED_ACCOUNT ? null : rate(stats.startOk, USERS),
  messagesSent: stats.sent,
  deliveriesExpected: expected,
  deliveriesReceived: stats.received,
  deliverySuccessPct: rate(stats.received, expected),
  pingTimeouts: ROOM_ID == null ? stats.pingTimeout : null,
  latencyMs: { avg: round(sorted.length ? sorted.reduce((sum, v) => sum + v, 0) / sorted.length : null), p50: round(percentile(sorted, 0.5)), p95: round(percentile(sorted, 0.95)), max: round(sorted[sorted.length - 1] ?? null) },
  locationSaveSuccessPct: rate(stats.locationSaveOk, stats.locationSaveOk + stats.locationSaveFail),
  runEndSuccessPct: IS_SHARED_ACCOUNT ? null : rate(stats.endOk, stats.startOk),
  recordSaveSuccessPct: IS_SHARED_ACCOUNT ? null : rate(stats.recordOk, stats.startOk),
  server: SERVER_PID ? {
    cpuAvgPct: round(cpu.avg),
    cpuMaxPct: round(cpu.max),
    memoryMaxMb: round(serverSamples.length ? Math.max(...serverSamples.map((sample) => sample.memoryMb)) : null),
  } : null,
  unexpectedErrors: stats.unexpected,
  errorSamples: [...new Set(stats.errors)].slice(0, 5),
};
console.log(JSON.stringify(result, null, 2));
process.exit(0);
