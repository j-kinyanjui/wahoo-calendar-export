# Pitfalls Research

**Domain:** GraphQL API Integration with JWT Authentication
**Researched:** 2026-03-01
**Confidence:** MEDIUM

## Critical Pitfalls

### Pitfall 1: JWT Token Not Validated Before GraphQL Requests

**What goes wrong:**
App sends GraphQL requests with user-provided JWT without validating the token's signature, expiration, or claims. Compromised or expired tokens grant unauthorized access to Systm API.

**Why it happens:**
Developers assume user-provided tokens are trusted because they come from the user. Missing validation step is a critical security flaw.

**How to avoid:**
Always validate JWT before use:
1. Verify signature matches the expected issuer
2. Check `exp` claim is not expired
3. Validate `iss` (issuer) matches expected value
4. Store expected signing key/algorithm securely

**Warning signs:**
- No error handling for expired tokens during API calls
- Token passed directly to headers without validation
- Missing try/catch around token parsing

**Phase to address:** Authentication Phase

---

### Pitfall 2: No Handling for Expired Tokens During Sync

**What goes wrong:**
User provides a JWT that expires during the app's runtime. When the token expires, the sync fails silently or crashes with an unhandled 401 error.

**Why it happens:**
Manual JWT input means tokens have fixed lifetimes (typically 1-24 hours). App doesn't track token validity or prompt for re-entry.

**How to avoid:**
1. Parse token's `exp` claim on input and store expiration time
2. Check token validity before making API calls
3. Provide clear error message when token expired: "Systm token expired. Please provide a new token."
4. Consider storing token in session with expiration metadata

**Warning signs:**
- No token expiration tracking
- Generic "API error" messages instead of specific token expiry guidance

**Phase to address:** Authentication Phase

---

### Pitfall 3: GraphQL Error Responses Ignored

**What goes wrong:**
App only checks HTTP status code (200 vs error) but ignores GraphQL response errors in the `errors` field. Partial failures go undetected.

**Why it happens:**
GraphQL always returns 200 OK even when query fails. Errors are in response body.

**How to avoid:**
Parse GraphQL response structure:
```kotlin
data class GraphQLResponse<T>(
    val data: T?,
    val errors: List<GraphQLError>?
)
// Check both data != null AND errors == null
```

**Warning signs:**
- No handling for `errors` field in GraphQL responses
- Only checking HTTP status codes
- Missing logging of GraphQL errors

**Phase to address:** Data Fetching Phase

---

### Pitfall 4: N+1 Query Problem in GraphQL Resolution

**What goes wrong:**
Fetching training plans triggers separate API call for each plan's details instead of bulk fetching. Causes slow performance.

**Why it happens:**
GraphQL resolvers make individual REST calls per item without batching. Systm API might have rate limits.

**How to avoid:**
1. Use DataLoader pattern to batch requests
2. Cache responses when appropriate
3. Design GraphQL schema to minimize nested queries
4. Monitor query count in development

**Warning signs:**
- Multiple identical API calls in logs
- Performance degrades with more plans
- API rate limiting errors

**Phase to address:** Data Fetching Phase

---

### Pitfall 5: Manual Token Input Not Persisted Securely

**What goes wrong:**
JWT token stored in plain text (localStorage, config files) exposing credentials if device compromised.

**Why it happens:**
Developers treat manual tokens as "user-provided" and skip security measures.

**How to avoid:**
1. Store tokens in encrypted session storage
2. Use Ktor's session encryption
3. Don't persist to plain config files
4. Clear token on logout

**Warning signs:**
- Token in plaintext logs
- Token stored in version-controlled files
- No encryption on session storage

**Phase to address:** Authentication Phase

---

### Pitfall 6: Big Bang GraphQL Migration

**What goes wrong:**
Replacing REST API with GraphQL entirely breaks existing functionality. No fallback, users experience downtime.

**Why it happens:**
Attempting to replace entire REST API in one release instead of incremental adoption.

**How to avoid:**
1. Run GraphQL in parallel with REST during transition
2. Feature flag GraphQL vs REST switching
3. Keep REST working until GraphQL proven stable
4. Test both paths in staging

**Warning signs:**
- No dual-api strategy planned
- Single release covers both REST removal and GraphQL addition

**Phase to address:** Integration Planning Phase

---

### Pitfall 7: No Rate Limiting Handling for GraphQL

**What goes wrong:**
App doesn't handle rate limit errors from Systm API. Causes sync failures under load.

**Why it happens:**
GraphQL queries can be complex. Systm API likely has rate limits that aren't handled gracefully.

**How to avoid:**
1. Implement retry with exponential backoff
2. Add circuit breaker pattern
3. Cache frequently requested data
4. Handle 429 responses specifically

**Warning signs:**
- No retry logic
- No rate limit error handling
- Sudden failures without clear cause

**Phase to address:** Data Fetching Phase

---

## Integration Gotchas

| Integration | Common Mistake | Correct Approach |
|-------------|----------------|------------------|
| Systm GraphQL | Not parsing nested `GetUserPlansRange` response | Map exact response structure with workout names, dates, IDs |
| JWT Token | Not validating token claims | Verify `exp`, `iss`, signature before use |
| Dual API | No unified error handling | Create abstraction for both REST and GraphQL errors |
| Manual Token | No token refresh mechanism | Track expiration, prompt user for new token |

---

## Security Mistakes

| Mistake | Risk | Prevention |
|---------|------|------------|
| Not validating JWT signature | Attacker forges valid token | Verify with known issuer/algorithm |
| Storing token in plain text | Credentials exposed if breach | Encrypt session storage |
| No token expiration check | Stolen token works forever | Parse `exp` claim, reject expired |
| Trusting token without verification | Compromised tokens accepted | Always validate full token |

---

## Performance Traps

| Trap | Symptoms | Prevention | When It Breaks |
|------|----------|------------|----------------|
| N+1 queries | One API call per plan | Batch requests with DataLoader | More than 10 plans |
| No caching | Repeated API calls | Cache plan metadata | Multiple syncs per day |
| Large query payload | Slow responses | Request only needed fields | Large training plans |

---

## "Looks Done But Isn't" Checklist

- [ ] **JWT Validation:** Token signature verified — verify with test expired token
- [ ] **GraphQL Errors:** Response `errors` field checked — verify with malformed query
- [ ] **Token Expiration:** Expiry tracked and communicated — verify with expired token input
- [ ] **Rate Limiting:** 429 errors handled — verify with rapid sync attempts
- [ ] **Dual API:** Both REST and GraphQL functional — verify fallback path works

---

## Recovery Strategies

| Pitfall | Recovery Cost | Recovery Steps |
|---------|---------------|----------------|
| Expired token during sync | LOW | Clear error message, prompt for new token |
| GraphQL errors ignored | MEDIUM | Add error parsing, re-sync affected data |
| N+1 performance | MEDIUM | Add batching, deploy update |
| Token validation missing | HIGH | Audit logs for unauthorized access, rotate tokens |

---

## Pitfall-to-Phase Mapping

| Pitfall | Prevention Phase | Verification |
|---------|------------------|--------------|
| JWT not validated | Authentication | Test with forged/expired token |
| Expired token handling | Authentication | Input expired token, verify error message |
| GraphQL errors ignored | Data Fetching | Send invalid query, verify error handling |
| N+1 queries | Data Fetching | Log API calls, verify batching |
| Token storage security | Authentication | Security audit of storage |
| Rate limiting | Data Fetching | Stress test sync |

---

## Sources

- [JWT Security Best Practices (JWT.app)](https://jwt.app/blog/jwt-best-practices/)
- [IETF JWT Best Current Practices](https://www.ietf.org/archive/id/draft-ietf-oauth-rfc8725bis-02.html)
- [GraphQL Security Issues - API Park](https://apipark.com/techblog/en/5-critical-graphql-security-issues-you-must-address-now/)
- [Common JWT Mistakes - Stackademic](https://blog.stackademic.com/common-jwt-mistakes-that-quietly-break-your-auth-2be0db4b2a9a)
- [Ktor JWT Documentation](https://ktor.io/docs/server-jwt.html)
- [GraphQL Kotlin Ktor Server](https://expediagroup.github.io/graphql-kotlin/docs/server/ktor-server/ktor-overview/)

---
*Pitfalls research for: Wahoo Plan to Calendar - Systm GraphQL Integration*
*Researched: 2026-03-01*
