# Copilot instructions

### Reporting threshold

Report only high-confidence defects introduced or worsened by
this PR that could cause:
- Security vulnerabilities or exposure of sensitive information.
- Data loss, corruption, or incorrect results.
- Production failures or performance degradation.
- Broken functionality and regressions.

Do not report minor style, formatting, naming, optimisations, optional
refactoring, or missing documentation/tests unless you identify a concrete
impactful defect. Do not report unrelated pre-existing issues.

### Investigating findings

- Read enough surrounding code and relevant call sites to verify the issue.
- Check existing guards, validation, configuration, and tests before reporting.
- Distinguish intentional behaviour changes from accidental regressions.
- Do not assume undocumented requirements, unsupported inputs, or hypothetical
  callers. Establish a realistic failure scenario from available evidence.
- If an important assumption cannot be verified, omit the finding rather
  than presenting speculation as a defect.

### Writing findings

- Identify the affected code using the smallest useful line range.
- Explain the triggering conditions and concrete user or system impact.
- Keep each comment concise and actionable.
- Suggest the smallest appropriate fix when the solution is clear.
- Report each root cause once; do not duplicate it across multiple locations.
- Do not manufacture findings or fill a comment quota.
- If nothing meets the reporting threshold, leave no inline findings.
