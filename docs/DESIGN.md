# Design notes

## Transactional outbox

`POST /v1/events` is handled by `EventController`. Request validation rejects
blank or malformed event types and non-object data. `EventPublisher` runs with
`Propagation.MANDATORY`, serializes the event envelope, and inserts both the
event and its `PENDING` outbox row using the caller's transaction.

This prevents a committed event without work, or an outbox row without its
payload. There is no broker or second write system in this stage.

## Polling

`OutboxPoller` runs on a fixed schedule and selects pending work with:

```sql
SELECT ...
FROM outbox
JOIN events ...
WHERE outbox.status = 'PENDING'
FOR UPDATE OF outbox SKIP LOCKED
LIMIT ?
```

The transaction holds row locks only for the poll operation. A second
application instance can therefore skip rows currently being inspected by
another poller. This stage records what was found but does not make an HTTP
request or change the row status.

## Deliberate scope

Endpoint management, HTTP sending, signing, retries, leases, dead-lettering,
replay, and delivery inspection are intentionally deferred to later reviews.
