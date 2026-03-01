# Feature Research

**Domain:** Training Plan Calendar Sync (GraphQL API Integration)
**Researched:** 2026-03-01
**Confidence:** HIGH

## Feature Landscape

### Table Stakes (Users Expect These)

Features users assume exist. Missing these = product feels incomplete.

| Feature | Why Expected | Complexity | Notes |
|---------|--------------|------------|-------|
| Manual JWT token input | Users need to authenticate with Systm GraphQL API; token must be obtained from DevTools | LOW | Simple text input with paste support; validate format before storing |
| GraphQL client setup | Required to query Systm API; existing Ktor HTTP client can wrap GraphQL | LOW | Can use graphql-kotlin-ktor-client or manual HTTP POST with JSON body |
| Fetch training plans query | Core feature - GetUserPlansRange query retrieves user's training plan items | MEDIUM | Requires date range parameters; query structure known from HAR analysis |
| Parse workout names and dates | Data transformation needed to display and sync workouts | LOW | Extract from GraphQL response structure |
| Display fetched plans | Users need to see what was fetched before syncing | LOW | Basic list view showing workout name, date, duration |
| Token storage (session/cookie) | Token persists across requests for subsequent fetches | LOW | Use Ktor sessions (existing pattern from Wahoo OAuth) |

### Differentiators (Competitive Advantage)

Features that set the product apart. Not required, but valuable.

| Feature | Value Proposition | Complexity | Notes |
|---------|-------------------|------------|-------|
| Token validation before use | Prevents confusing errors when expired/invalid token is entered | MEDIUM | Make a lightweight query (e.g., user info) to verify token works |
| Date range selector UI | Users can choose which training plans to fetch | MEDIUM | Date pickers for start/end; defaults to next 30 days |
| Workout type filtering | Users may only want cycling or running workouts | LOW | Checkbox or multi-select UI |
| Plan preview before sync | Shows exactly what will be synced to calendar | MEDIUM | Summary count + sample items |
| Multiple token support | Users with multiple Systm accounts | MEDIUM | Store tokens keyed by user identifier |

### Anti-Features (Commonly Requested, Often Problematic)

Features that seem good but create problems.

| Feature | Why Requested | Why Problematic | Alternative |
|---------|---------------|-----------------|-------------|
| OAuth2 flow for Systm | Seems more "proper" than manual token | Adds significant complexity; Systm doesn't support standard OAuth; user can extract token from DevTools | Keep manual token input as documented |
| Real-time sync | "I want workouts to appear instantly" | Requires WebSocket/polling infrastructure; overkill for training plans which are weekly/monthly cadence | On-demand fetch + daily scheduled sync is sufficient |
| Calendar export in v1 | Seem like logical next step | Scope creep; delays core validation; can add later | Defer to v1.x after validating core fetch works |
| Automatic token refresh | Tokens expire and need renewal | Systm tokens are long-lived; refresh adds complexity not needed for manual input | Ask user to input new token when needed |

## Feature Dependencies

```
[Manual JWT Token Input]
    └──requires──> [Token Storage]

[Token Storage]
    └──requires──> [GraphQL Client Setup]

[GraphQL Client Setup]
    └──requires──> [Fetch Training Plans Query]

[Fetch Training Plans Query]
    └──requires──> [Parse Workout Data]

[Parse Workout Data]
    └──requires──> [Display Fetched Plans]

[Token Validation] ──enhances──> [Manual JWT Token Input]
[Date Range Selector] ──enhances──> [Fetch Training Plans Query]
```

### Dependency Notes

- **Token Input requires Token Storage:** Input must be persisted for subsequent requests
- **GraphQL client must be configured before queries:** Client setup includes endpoint, headers (Authorization Bearer)
- **Parse is downstream of Fetch:** Can't parse without first fetching the data
- **Token validation enhances token input:** Provides immediate feedback instead of waiting for fetch to fail

## MVP Definition

### Launch With (v1)

Minimum viable product — what's needed to validate the concept.

- [x] Manual JWT token input — core authentication mechanism
- [x] GraphQL client with GETUSERPLANS query — fetches training plans from Systm
- [x] Parse workout names and dates — extracts needed data from GraphQL response
- [x] Display fetched plans — shows user what was retrieved before sync

### Add After Validation (v1.x)

Features to add once core is working.

- [ ] Token validation — immediate feedback on token validity
- [ ] Date range selector — more control over fetched plans
- [ ] Workout type filtering — reduce noise from unwanted workout types

### Future Consideration (v2+)

Features to defer until product-market fit is established.

- [ ] Calendar export — explicit calendar file generation
- [ ] Scheduled daily sync — background job for automatic fetches
- [ ] Multiple account support — manage multiple Systm users

## Feature Prioritization Matrix

| Feature | User Value | Implementation Cost | Priority |
|---------|------------|---------------------|----------|
| Manual JWT token input | HIGH | LOW | P1 |
| GraphQL client setup | HIGH | LOW | P1 |
| Fetch training plans | HIGH | MEDIUM | P1 |
| Parse workout data | HIGH | LOW | P1 |
| Display fetched plans | HIGH | LOW | P1 |
| Token validation | MEDIUM | MEDIUM | P2 |
| Date range selector | MEDIUM | MEDIUM | P2 |
| Workout type filtering | LOW | LOW | P2 |
| Plan preview | MEDIUM | MEDIUM | P2 |
| Multiple token support | LOW | MEDIUM | P3 |

**Priority key:**
- P1: Must have for launch
- P2: Should have, add when possible
- P3: Nice to have, future consideration

## Competitor Feature Analysis

| Feature | suffersync (Python) | Wahoo SYSTM App | Our Approach |
|---------|---------------------|------------------|--------------|
| Manual token input | Uses username/password | OAuth only | Manual JWT input (simpler auth, no OAuth needed) |
| Date range selection | Config file based | In-app UI | UI-based selector (v1.x) |
| Workout filtering | Config file | Yes | Checkbox filter (v1.x) |
| Calendar sync | intervals.icu only | Internal only | Display first, calendar export later |

## Sources

- GraphQL Kotlin Ktor client documentation: https://expediagroup.github.io/graphql-kotlin/docs/client/client-overview
- suffersync (reference implementation): https://github.com/bakermat/suffersync
- JWT storage best practices: https://www.syncfusion.com/blogs/post/secure-jwt-storage-best-practices
- Project context: .planning/PROJECT.md

---

*Feature research for: Wahoo Plan to Calendar - Systm GraphQL Integration*
*Researched: 2026-03-01*
