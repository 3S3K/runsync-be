#!/usr/bin/env node
/**
 * RunSync 동시 사용자 부하 테스트. 외부 패키지 없이 Node 22 이상에서 실행한다.
 *
 * 가상 사용자마다: WebSocket 접속 → 러닝룸(협동 러닝) 구독 → 러닝 시작 → GPS 송수신 → 러닝 종료 → 기록 저장
 *
 *   # 가상 계정 모드: 계정마다 토큰을 직접 만든다 (서버와 같은 JWT_SECRET 필요, 로컬 환경용)
 *   JWT_SECRET=... node load-test.mjs --target http://localhost:8090 --users 50 --first-user-id 2 --room 1
 *
 *   # 단일 계정 모드: 한 계정의 토큰으로 연결 수만 늘린다 (러닝 세션·러닝룸 없이 접속과 송수신만)
 *   ACCESS_TOKEN=... node load-test.mjs --target https://api.example.com --users 50
 */
import { execFileSync } from 'node:child_process';
import { createHmac } from 'node:crypto';
import { setTimeout as sleep } from 'node:timers/promises';

const args = Object.fromEntries(
  process.argv.slice(2).reduce((pairs, value, index, all) => (
    value.startsWith('--') ? [...pairs, [value.slice(2), all[index + 1]]] : pairs
  ), []),
);
const TARGET = (args.target || 'http://localhost:8090').replace(/\/$/, '');
const USERS = Number(args.users || 5);
const DURATION_SEC = Number(args.duration || 30);
const INTERVAL_MS = Number(args.interval || 1000);        // GPS 전송 주기
const FIRST_USER_ID = Number(args['first-user-id'] || 1);
const ROOM_ID = args.room ? Number(args.room) : null;      // 협동 러닝(러닝룸) 번호
const SERVER_PID = args['server-pid'] || null;             // 주면 서버 프로세스의 CPU·메모리를 함께 잰다
const JWT_SECRET = process.env.JWT_SECRET || '';
const SHARED_TOKEN = process.env.ACCESS_TOKEN || '';
const IS_SHARED_ACCOUNT = !JWT_SECRET;

if (IS_SHARED_ACCOUNT && !SHARED_TOKEN) {
  console.error('JWT_SECRET(가상 계정 모드) 또는 ACCESS_TOKEN(단일 계정 모드) 환경변수가 필요합니다.');
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
      const timer = setTimeout(() => reject(new Error('연결 시간 초과')), 10000);
      this.socket = new WebSocket(WS_URL);
      this.socket.onopen = () => this.frame('CONNECT', {
        'accept-version': '1.2', 'heart-beat': '0,0', Authorization: `Bearer ${this.token}`,
      });
      this.socket.onerror = () => { clearTimeout(timer); reject(new Error('소켓 오류')); };
      this.socket.onclose = () => { this.closed = true; clearTimeout(timer); reject(new Error('연결 종료')); };
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
            clearTimeout(timer);
            resolve();
          } else if (command === 'ERROR') {
            this.errors.push(headers.message || payload);
            clearTimeout(timer);
            reject(new Error(headers.message || 'STOMP 오류'));
          } else if (command === 'MESSAGE') {
            this.handlers.get(headers.subscription)?.(payload);
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
  locationSaveOk: 0, locationSaveFail: 0,
  endOk: 0, endFail: 0,
  recordOk: 0, recordFail: 0,
  errors: [],
};
const sentAt = new Map();      // "보낸 사용자:위도" → 보낸 시각. 받은 쪽에서 지연 시간을 계산한다
const cpuSamples = [];
const memorySamples = [];

function fail(kind, error) {
  stats[kind] += 1;
  if (stats.errors.length < 10) {
    stats.errors.push(error.message);
  }
}

function sampleServer() {
  if (!SERVER_PID) {
    return;
  }
  try {
    const [cpu, rssKb] = execFileSync('ps', ['-o', '%cpu=,rss=', '-p', SERVER_PID]).toString().trim().split(/\s+/);
    cpuSamples.push(Number(cpu));
    memorySamples.push(Number(rssKb) / 1024);
  } catch {
    // 프로세스를 찾지 못하면 건너뛴다
  }
}

// ----- 가상 사용자 한 명 -----

async function runUser(index, startSignal) {
  const userId = FIRST_USER_ID + index;
  const token = IS_SHARED_ACCOUNT ? SHARED_TOKEN : mintToken(userId);
  const sender = IS_SHARED_ACCOUNT ? `c${index}` : String(userId);
  const client = new StompClient(token);

  try {
    await client.connect();
    stats.connectOk += 1;
  } catch (error) {
    fail('connectFail', error);
    return;
  }

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
  } else {
    // 러닝룸이 없으면 서버의 핑 응답으로 왕복 시간을 잰다
    client.subscribe('/user/queue/ping', 'ping', () => {
      const sent = sentAt.get(`${sender}:ping`);
      if (sent) {
        stats.received += 1;
        stats.latencies.push(performance.now() - sent);
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

  await startSignal;      // 모든 사용자가 접속을 마친 뒤 동시에 달리기 시작한다

  const ticks = Math.round((DURATION_SEC * 1000) / INTERVAL_MS);
  for (let tick = 0; tick < ticks && !client.closed; tick += 1) {
    const latitude = Number((BASE.lat + index * 0.001 + tick * 0.00001).toFixed(6));
    const longitude = Number((BASE.lng + index * 0.001).toFixed(6));

    if (ROOM_ID != null) {
      sentAt.set(`${userId}:${latitude.toFixed(6)}`, performance.now());
    } else {
      sentAt.set(`${sender}:ping`, performance.now());
      client.send('/app/ping', {});
    }
    client.send('/app/location', {
      type: 'LOCATION_UPDATE',
      data: { ...(sessionId ? { sessionId } : null), ...(ROOM_ID != null ? { artRunSessionId: ROOM_ID } : null), latitude, longitude },
    });
    stats.sent += 1;

    // 실제 앱처럼 5초마다 누적 거리·시간을 저장한다
    if (sessionId && tick > 0 && tick % 5 === 0) {
      api(token, 'PATCH', `/api/run-sessions/${sessionId}/location`, {
        lastLatitude: latitude, lastLongitude: longitude, currentDistance: Number((tick * 0.003).toFixed(2)), currentDurationTime: tick,
      }).then(() => { stats.locationSaveOk += 1; }).catch((error) => fail('locationSaveFail', error));
    }
    await sleep(INTERVAL_MS);
  }

  await sleep(1500);      // 마지막 메시지가 도착할 시간을 준다

  if (sessionId) {
    try {
      await api(token, 'PATCH', `/api/run-sessions/${sessionId}`, {
        endTime: new Date().toISOString(), totalDistance: Number((ticks * 0.003).toFixed(2)),
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

let releaseStart;
const startSignal = new Promise((resolve) => { releaseStart = resolve; });
const sampler = setInterval(sampleServer, 2000);

const users = [];
for (let index = 0; index < USERS; index += 1) {
  users.push(runUser(index, startSignal));
  await sleep(20);      // 접속이 한 순간에 몰리지 않게 조금씩 띄운다
}
await sleep(1500);
const connectedAtStart = stats.connectOk;
releaseStart();
await Promise.all(users);
clearInterval(sampler);

const sorted = [...stats.latencies].sort((a, b) => a - b);
// 러닝룸에서는 보낸 위치 하나를 접속한 참가자 모두가 받는다. 핑은 보낸 사람만 받는다
const expected = ROOM_ID != null ? stats.sent * connectedAtStart : stats.sent;
const round = (value) => (value == null ? null : Number(value.toFixed(1)));

const result = {
  target: TARGET,
  mode: IS_SHARED_ACCOUNT ? '단일 계정' : '가상 계정',
  users: USERS,
  durationSec: DURATION_SEC,
  connectSuccessPct: rate(stats.connectOk, USERS),
  runStartSuccessPct: IS_SHARED_ACCOUNT ? null : rate(stats.startOk, USERS),
  messagesSent: stats.sent,
  deliveriesExpected: expected,
  deliveriesReceived: stats.received,
  deliverySuccessPct: rate(stats.received, expected),
  latencyMs: { avg: round(sorted.reduce((sum, v) => sum + v, 0) / (sorted.length || 1)), p50: round(percentile(sorted, 0.5)), p95: round(percentile(sorted, 0.95)), max: round(sorted[sorted.length - 1] ?? null) },
  locationSaveSuccessPct: rate(stats.locationSaveOk, stats.locationSaveOk + stats.locationSaveFail),
  runEndSuccessPct: IS_SHARED_ACCOUNT ? null : rate(stats.endOk, stats.startOk),
  recordSaveSuccessPct: IS_SHARED_ACCOUNT ? null : rate(stats.recordOk, stats.startOk),
  server: SERVER_PID ? {
    cpuAvgPct: round(cpuSamples.reduce((sum, v) => sum + v, 0) / (cpuSamples.length || 1)),
    cpuMaxPct: round(Math.max(0, ...cpuSamples)),
    memoryMaxMb: round(Math.max(0, ...memorySamples)),
  } : null,
  errorSamples: [...new Set(stats.errors)].slice(0, 5),
};
console.log(JSON.stringify(result, null, 2));
process.exit(0);
