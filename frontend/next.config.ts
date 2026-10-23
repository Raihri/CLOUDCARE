import type { NextConfig } from "next";

/**
 * Spring Boot base URL. Server-side fetches talk to this directly.
 * Override with SPRING_API_URL in .env.local (e.g. for a remote backend).
 */
const SPRING_API_URL = process.env.SPRING_API_URL ?? "http://localhost:8080";

const nextConfig: NextConfig = {
  cacheComponents: true,
  partialPrefetching: true,
  experimental: {
    serverActions: {
      // The doctor application uploads a profile photo and a certificate in
      // one Server Action. The framework default is 1 MB, while Spring accepts
      // up to 20 MB per file; without this the form fails on any normal phone
      // photo. Leave room for multipart boundaries and field metadata.
      bodySizeLimit: "22mb",
    },
  },
  async rewrites() {
    return [
      {
        // Same-origin proxy to Spring. Because the browser only ever talks to
        // :3000, CORS becomes a non-issue and JSESSIONID stays a first-party
        // cookie. Needed for the few cases that require a browser-side call
        // (e.g. streaming a PDF download).
        source: "/api/spring/:path*",
        destination: `${SPRING_API_URL}/:path*`,
      },
    ];
  },
  turbopack: {
    rules: {
      "*.css": {
        loaders: ["@tailwindcss/turbopack"],
        as: "*.css",
      },
    },
  },
};

export default nextConfig;