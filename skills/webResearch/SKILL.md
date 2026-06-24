---
name: web research
description: Search the web and fetch specific pages to answer current, source-dependent, or URL-based questions.
version: 1.0.0
---

# Web Research

Use this skill when the user asks for current, external, source-dependent, price-related, policy-related, API-related, version-related, news-related, or citation-required information.

## Available Tools

- `web_search`: Search the public web and return candidate pages.
- `web_fetch`: Fetch and extract readable content from a known URL.


## When to Use

Use this skill when:
- The user asks for latest, current, today, recent, price, availability, policy, regulation, changelog, release date, news, ranking, or comparison.
- The answer depends on external sources.
- The model is uncertain or the information may have changed.
- The user asks for sources, citations, or URLs.

Do not use this skill when:
- The task is translation, rewriting, formatting, or brainstorming.
- The answer is stable common knowledge.
- The user explicitly says not to search.

## Procedure

1. Decide whether web access is required.
2. Generate 1 to 3 search queries.
3. Call `web_search` with the strongest query first.
4. Prefer official, primary, recent, and authoritative sources.
5. Do not answer from snippets alone unless the user only asks for navigation links.
6. For each important source, call `web_fetch` on the URL.
7. Extract:
    - title
    - source
    - publication/update date
    - key claims
    - relevant evidence
8. Filter low-quality results:
    - duplicated pages
    - SEO spam
    - pages without dates for time-sensitive claims
    - copied content without original source
9. Cross-check important claims with at least two sources when possible.
10. If pages require JavaScript or login, use `browser` instead of `web_fetch`.

## Source Priority

Prefer sources in this order:

1. Official documentation, official website, government source, company announcement
2. Standards, papers, GitHub repositories, package registries
3. Reputable media or industry publications
4. Blogs, forums, Reddit, social posts

## Filtering Rules

Reject or deprioritize:
- Sources with no author/source/date for time-sensitive claims
- Content farms
- Aggregated snippets without original links
- Pages that only quote another source
- Outdated pages when the query asks for latest/current information

## Stop Conditions

Stop searching when:
- The answer is supported by reliable sources.
- Additional results are repetitive.
- You have checked 2 to 3 strong sources.
- You have done 2 to 3 search rounds without improving confidence.

## Output Rules

- Answer first.
- Include sources.
- Mention dates when time matters.
- If sources conflict, explain the conflict.
- If reliable information is not found, say so clearly.