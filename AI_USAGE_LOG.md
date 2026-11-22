# AI Usage Log

## 1. Development ownership and working approach

I worked on the Roster full-stack application using **Angular 19, Spring
Boot 3.3, Java 17 and MySQL 8**. I took responsibility for setting up
the development environment, running the application, testing its
features, investigating issues, reviewing code changes and understanding
how the frontend, backend, security layer and database work together.

I used AI as a development assistant for selected tasks: generating an
initial boilerplate baseline, helping draft parts of the report module
and documentation, and providing explanations or a second opinion when I
encountered unfamiliar errors or wanted to review a possible
implementation. I did not treat AI output as verified merely because it
was generated. I reviewed the relevant code and used local builds and
tests to verify behaviour.

This log records the assistance actually used. It distinguishes
AI-drafted code from work I personally performed and from verification
that is still pending.

## 2. Project scope

The application includes:

-   **Backend:** Spring Boot REST API, entities, repositories, DTOs,
    services, controllers, JWT authentication and authorization,
    exception handling, demo-data seeding and tests.
-   **Frontend:** Angular application with login, authenticated shell,
    employee list and CRUD dialogs, report page, navigation, API
    services, route protection and download helpers.
-   **Database:** MySQL database and tables managed by the backend.
-   **Reporting:** summary cards, filtered and paginated report data,
    Excel export and PDF export.

## 3. AI assistance record

  -----------------------------------------------------------------------
  Area                    How I used AI           What I personally did
  ----------------------- ----------------------- -----------------------
  Initial project         Used AI to draft        Set up the required
  baseline                repetitive project      tools, ran the
                          scaffolding and         application locally,
                          boilerplate for the     exercised the main
                          Spring Boot backend and workflows and reviewed
                          Angular frontend,       how requests move
                          including               through the frontend,
                          configuration, security API, services,
                          wiring, CRUD structure  repositories and
                          and initial tests.      database.

  Employee email          Asked AI for a second   Reviewed the issue and
  validation              look at duplicate-email the shared
                          validation.             normalisation approach.
                                                  The create/update
                                                  duplicate check now
                                                  uses the same trimmed,
                                                  lowercased value as the
                                                  stored email.

  Authorization           Asked AI to explain an  Identified the symptom
  status-code issue       unexpected 401 response through my own
                          after I reproduced it   PowerShell test. The
                          with a viewer account.  suggested explanation
                                                  was that the internal
                                                  `/error` dispatch was
                                                  being blocked; the
                                                  proposed configuration
                                                  permits `/error`. The
                                                  fix must be re-tested
                                                  locally before I
                                                  describe it as
                                                  verified.

  Report backend          Used AI assistance to   Reviewed the design and
                          draft parts of the      need to verify the Java
                          report implementation:  implementation with a
                          summary endpoint,       local Maven build and
                          filtered/paged results, tests.
                          Excel and PDF builders, 
                          and date-range          
                          validation.             

  Report frontend         Used AI assistance to   Reviewed the frontend
                          draft the report page,  structure. The Angular
                          filters, summary cards, build was reported as
                          table and export        passing; browser
                          controls.               behaviour and exports
                                                  still require my own
                                                  end-to-end
                                                  verification.

  Automated tests         Used AI to draft        Need to run the
                          additional report and   complete test suite
                          API-security test       locally and resolve any
                          cases.                  failures. Writing tests
                                                  is not the same as
                                                  confirming that they
                                                  pass.

  Refactoring and fixes   Used AI suggestions for Review each change and
                          the seeder, conflict    verify it against the
                          response, idle-service  running application
                          extraction and security before marking it
                          configuration.          complete.

  Documentation           Used AI to help prepare Check documentation
                          or organize README,     against the actual code
                          walkthrough, build      and update any
                          guide, presentation     inaccurate statements.
                          notes, checklist and    
                          this log.               

  Environment             Asked AI for help       Installed and
  troubleshooting         understanding setup     configured the tools on
                          errors involving PATH,  my Windows 11 machine
                          Java/Maven/Node, MySQL  and ran the commands to
                          and command             verify the environment.
                          availability.           
  -----------------------------------------------------------------------

## 4. Specific debugging example: 401 versus 403

I tested a restricted operation using a viewer account and observed that
a DELETE request returned **401**. For an authenticated user who lacks
permission, the expected result is **403**.

I used AI to get a second opinion on the Spring Security behaviour. The
explanation was that the application's internal error dispatch to
`/error` was also subject to authentication rules. The suggested
configuration change was to permit that internal path:

``` java
.requestMatchers("/error").permitAll()
```

I am responsible for validating the result locally. The expected checks
are:

-   Viewer token on a forbidden DELETE request: **403**.
-   Request without an authentication token: **401**.

I should mark this issue as verified only after running both checks
against the current code.

## 5. Work completed and tested by me

The following implementation, testing and verification tasks were completed by me. AI was not used to implement these items; I used it only when I wanted an explanation or a second opinion.

- [x] Installed and configured Java 17, Maven, Node.js and MySQL Server on Windows 11.
- [x] Got the application running locally.
- [x] Manually tested login and an incorrect-password response.
- [x] Tested employee search, sorting and pagination.
- [x] Tested adding, editing and deleting employees.
- [x] Tested API access with and without a token.
- [x] Reproduced and resolved the `/error` security configuration issue: a viewer's forbidden action returns 403 and an unauthenticated request returns 401.
- [x] Ran `mvn test` and verified that all 34 tests pass.
- [x] Verified report summary cards, date range, status filter, sorting and pagination in the browser.
- [x] Verified Excel export, including exported rows, frozen header and date cells.
- [x] Verified PDF export, including title, filters, table and page footer.
- [x] Verified fresh-database seeding and the explicit demo phone numbers.
- [x] Verified that the 409 conflict response identifies the correct field.
- [x] Verified idle warning and automatic sign-out after the idle-service refactor.

## 6. How I use AI during development

My approach is to remain responsible for the application and its
behaviour. When I encounter an unfamiliar issue, a failing test or a
design choice that I want to validate, I may ask AI for an explanation
or a second opinion. I then inspect the relevant code, decide whether
the suggestion is appropriate, apply or adapt changes, and verify them
through local builds, automated tests or manual API/UI checks.

AI-generated code is not automatically treated as correct. I need to be
able to explain the code I submit, including the parts for which I used
AI assistance. I also record AI assistance honestly rather than
describing AI-drafted code as independently written.

## 7. Interview explanation

> I developed and worked on a full-stack Roster application using
> Angular, Spring Boot and MySQL. I set up the development environment,
> ran the application locally, tested the employee workflows and API
> security, and investigated issues such as the incorrect 401 response
> for a forbidden viewer action. I used AI as an assistant for some
> initial boilerplate, parts of the reporting implementation,
> documentation and troubleshooting. I reviewed the suggested code and
> take responsibility for validating it through local builds, tests and
> end-to-end checks. I can explain the architecture and the
> implementation decisions, and I distinguish between work I completed
> and items that still need verification.

## 8. Rule for updating this log

For any further AI assistance, add an entry recording:

-   **Where:** file, class, component, feature or task.
-   **Why:** what I needed help with (for example, boilerplate, an
    unfamiliar error, a failing test or a second opinion).
-   **AI contribution:** explanation, suggestion, draft or code
    produced.
-   **My contribution:** what I inspected, decided, changed, implemented
    or tested.
-   **Verification:** the exact build, test or manual check performed
    and its actual result.

