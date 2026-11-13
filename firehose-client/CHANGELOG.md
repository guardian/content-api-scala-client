# v0.3.0

- Updates library dependencies to AWS SDK v2 and AWS KCL v2
- Updates Scrooge to 21.1.0 from 19.3.0
- Add build for Scala 2.13
- Remove support for Scala 2.11, as Scrooge has dropped support for that
- Update content API data models
- Support new `EventPayload.DeletedContent` event. This introduces a new `contentDelete` method in the client's processor that must be implemented