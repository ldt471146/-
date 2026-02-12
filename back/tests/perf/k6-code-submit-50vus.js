import http from "k6/http";
import { check, sleep } from "k6";
import { Rate, Trend } from "k6/metrics";

const submitSuccess = new Rate("submit_success");
const submitLatency = new Trend("submit_latency");

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const LOGIN_EMAIL = __ENV.LOGIN_EMAIL || "student@example.com";
const LOGIN_PASSWORD = __ENV.LOGIN_PASSWORD || "123456";
const PROBLEM_ID = Number(__ENV.PROBLEM_ID || "1");
const LANGUAGE_ID = Number(__ENV.LANGUAGE_ID || "71");
const SOURCE_CODE = __ENV.SOURCE_CODE || "print(1)";

export const options = {
  vus: 50,
  duration: "30s",
  thresholds: {
    http_req_failed: ["rate<0.01"],
    http_req_duration: ["p(95)<3000"],
    submit_success: ["rate>0.99"],
    submit_latency: ["p(95)<3000"],
  },
};

export function setup() {
  const loginRes = http.post(
    `${BASE_URL}/api/auth/login`,
    JSON.stringify({
      email: LOGIN_EMAIL,
      password: LOGIN_PASSWORD,
      remember: false,
    }),
    { headers: { "Content-Type": "application/json" } }
  );

  const ok = check(loginRes, {
    "login status is 200": (r) => r.status === 200,
    "login response code is 0": (r) => r.json("code") === 0,
    "login token exists": (r) => !!r.json("data.token"),
  });

  if (!ok) {
    throw new Error(`login failed: status=${loginRes.status} body=${loginRes.body}`);
  }
  return { token: loginRes.json("data.token") };
}

export default function (data) {
  const submitRes = http.post(
    `${BASE_URL}/api/code/submit`,
    JSON.stringify({
      problemId: PROBLEM_ID,
      languageId: LANGUAGE_ID,
      sourceCode: SOURCE_CODE,
    }),
    {
      headers: {
        "Content-Type": "application/json",
        Authorization: `Bearer ${data.token}`,
      },
      tags: { api: "code_submit" },
    }
  );

  submitLatency.add(submitRes.timings.duration);

  const ok = check(submitRes, {
    "submit status is 200": (r) => r.status === 200,
    "submit response code is 0": (r) => r.json("code") === 0,
    "submit has result payload": (r) => !!r.json("data"),
  });
  submitSuccess.add(ok);
  sleep(0.2);
}
