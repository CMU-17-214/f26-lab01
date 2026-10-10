# Lab 1 Starter: Booking Service

A small room-booking service. Users book rooms for time intervals, and if a room is
taken they land on a waitlist. It is the codebase you work in for Lab 1.

**Read `ARCHITECTURE.md` first.** It maps the three layers (domain / service / repo)
so you do not have to cold-read every file.

## Build and test

```
mvn test
```

One test fails on purpose. Diagnosing and fixing it is Milestone 1.
By the way: the fix may not be where the failing test first points you. :-)

## Where things are

- Source: `src/main/java/edu/cmu/cs214/booking/` (`domain/`, `service/`, `repo/`)
- Tests: `src/test/java/edu/cmu/cs214/booking/`
- Setup: `SETUP.md`
- Your Milestone 2 task: `TASK.md`
- A proposed change you will review in Milestone 3: `changes/agent-attempt.patch` (the handout tells you how to apply it)
- Transcript export script (for Claude Code; modify if using a different tool): `tools/export-transcripts.sh` (the handout tells you when to run it)

See the Lab 1 handout on the course page for the three milestones you show a TA.
## 工具与模型声明
本项目实验使用AI大模型辅助构思、生成与审查代码。
所有AI生成代码均经过小组人工审阅、校验、补充测试用例，人工验证正确性后才纳入仓库。
会话记录transcripts文件夹仅本地保存，不提交至GitHub远程仓库。
