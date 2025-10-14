import http from 'k6/http';
import { sleep, check } from 'k6';

export const options = {
  vus: 50,
  duration: '1m',
  thresholds: {
    http_req_duration: ['p(95)<1000'], // p95 < 1s
    http_req_failed: ['rate<0.01'],
  },
};

const BASE = __ENV.BASE_URL || 'http://localhost:8080/api';

export default function () {
  const resProjects = http.get(`${BASE}/projects`);
  check(resProjects, { '200 /projects': (r) => r.status === 200 });

  const resDefects = http.get(`${BASE}/defects`);
  check(resDefects, { '200 /defects': (r) => r.status === 200 });

  const resAnalytics = http.get(`${BASE}/reports/analytics`);
  check(resAnalytics, { '200 /reports/analytics': (r) => r.status === 200 });

  sleep(1);
}
