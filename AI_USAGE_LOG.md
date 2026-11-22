# AI Usage Log

Rule from the recruiter: AI writes **template / boilerplate code only**. The candidate streamlines it, makes it more
efficient, and can explain every line. Each AI use must record: **where**, **why AI instead of doing it directly**,
and **what result AI achieved**. Also record what I changed afterwards.

Write each entry at the time you use AI, in your own words. Do not backfill generic text.

## Summary table (fill as you go)
| # | Where (file / feature) | Why AI instead of doing it myself | Result AI achieved | What I changed afterwards and why |
|---|---|---|---|---|
| 1 | Project scaffold: pom.xml, angular.json, tsconfig, package.json | Config boilerplate with many version-sensitive settings; typing it by hand adds no value | Both projects structured so they build and run | _TODO_ |
| 2 | JPA entities, repositories, DTO records | Repetitive getters / column mappings | Employee + user tables mapped, validation annotations in place | _TODO_ |
| 3 | JWT security (SecurityConfig, JwtService, JwtAuthFilter) | Filter-chain wiring is easy to get subtly wrong; wanted a working baseline to study | Stateless login, token validation, 401/403 behaviour | _TODO_ |
| 4 | Employee CRUD endpoints + service | Standard REST/pagination template | GET/POST/PUT/DELETE with paging, sort whitelist, duplicate check | _TODO_ |
| 5 | Angular login, shell, employees table, dialogs | Material component templates are long and mechanical | Working screens with validation, paging, sorting, search, toasts | _TODO_ |
| 6 | Unit tests (EmployeeServiceTest, JwtServiceTest) | Mockito boilerplate | 9 tests covering service rules and JWT validity | _TODO_ |
| 7 | README / API table | Documentation scaffolding | Setup steps and endpoint table | _TODO_ |

## Detailed entries
Copy this block for every AI interaction (including small questions like "why does this fail?").

### Entry N - <short title>
- **Tool:** (Claude / Copilot / ChatGPT)
- **Where I used it:** file(s) and feature
- **Prompt:** what I asked (paste or summarise)
- **Why AI instead of writing it directly:**
- **What AI produced:**
- **What result it achieved:** (e.g. "saved ~1h of config", "gave me a working JWT baseline")
- **Review and changes I made:** what was wrong, slow, or unclear, and how I improved it
- **What I learned:**
