# study18 · AI 연동·MCP

## ① 목표

문서 요약과 근거 답변, 토큰 스트리밍, MCP 도구 호출을 Vaadin 앱의 미니 예제로 붙이고 DocuForge의 흐름과 연결한다.

## ② 📖 이론

[DocuForge](https://app.notion.com/p/40bd7dec5e2e818d9c91c6fb26dccc4c)는 문서를 올리면 로컬 모델이 요약하고, 문서에서 고른 발췌만 근거로 답한다. 발췌에 없으면 찾을 수 없다고 말한다. 이 저장소는 그 화면과 서버를 가져오지 않는다. 같은 흐름만 Vaadin에서 짧게 만든다. 입력은 붙여 넣은 텍스트이고, 기본 모델 호출은 없다.

`SummaryClient`의 `mock`은 앞 120자를 요약으로 쓰고, 질문은 문서 문장에 단어가 있을 때만 그 문장을 돌려준다. `AI_PROVIDER=http`이고 `AI_API_URL`이 있으면 그 주소로 JSON을 POST한다. 키는 `AI_API_KEY`다. 실패하면 `AI 응답을 가져오지 못했습니다.`만 반환한다. 스택 트레이스를 사용자에게 주지 않는다.

스트리밍은 요약 문장을 잘게 나누어 `GET /api/ai/stream`이 `text/event-stream`으로 보낸다. 화면 `/tools`는 한 번에 받은 답을 보여 주고, 스트림은 API로 확인한다.

MCP 미니 서버는 `McpStudyServer`다. 표준 입력의 JSON-RPC 한 줄에 `tools/list`와 `tools/call`의 `search_document`로 답한다. 학습 문서에 없는 질문은 `문서에서 찾을 수 없습니다.`다. 이 파서는 학습용이라 중첩 JSON 전체를 해석하지 않는다.

프롬프트는 짧게 둔다. 요약은 문서 문장만 사용하고, 문서 밖 지식으로 채우지 말라는 규칙을 코드의 검색이 대신한다. 외부 모델을 켤 때도 같은 규칙을 프롬프트에 적는다.

비유하면 문서는 교과서이고, 모델은 교과서 밖으로 나가면 감점인 학생이다.

## ③ 🛠️ 실기

로그인 후 `/tools`에 아래를 붙여 넣는다.

```text
Vaadin Flow는 서버가 화면 상태를 세션에 둔다.
Hilla는 BrowserCallable로 Java를 호출한다.
```

질문 `Hilla`는 두 번째 문장을 돌려주고, 질문 `조리법`은 찾을 수 없다는 문장을 돌려준다.

스트림:

```powershell
# 토큰 필요
Invoke-WebRequest "http://localhost:8080/api/ai/stream?text=Vaadin%20Flow" -Headers $headers
```

MCP:

```powershell
Set-Location lesson
'{"jsonrpc":"2.0","id":1,"method":"tools/call","params":{"arguments":{"query":"Hilla"}}}' | mvn -q -DskipTests exec:java "-Dexec.mainClass=study.vaadin.ai.McpStudyServer"
```

`exec` 플러그인을 POM에 넣지 않았다면 테스트를 실행한다. `McpStudyServerTest`가 같은 검색을 검증하며 `mvn test`에서 통과했다.

외부 모델을 쓰려면 `.env`에 `AI_PROVIDER=http`, `AI_API_URL`, `AI_MODEL`을 넣는다. Ollama를 쓰는 경우 URL은 장비의 생성 API 주소이고, DocuForge 노트에 적힌 `gemma4:12b`를 `AI_MODEL`의 기본값으로 두었다. 모델이 없으면 mock을 유지한다.

## ④ 🔗 연동 포인트

DocuForge의 업로드와 요약, 발췌 답변은 `/tools`의 텍스트 입력, `SummaryClient`, MCP `search_document`에 대응한다. 청크 파일과 PostgreSQL, Vue, Rails, FastAPI는 이 저장소에 없다. studyFinal은 이 미니 예제를 게시판 서비스의 한 메뉴로 포함한다. 업로드한 파일을 요약에 넘기려면 study13의 저장 경로에서 텍스트를 읽어 `summarize`에 전달하면 된다. 바이너리 PDF 해석은 범위 밖이다.

## ⑤ 오류 노트

| 증상 | 원인 | 해결 |
| --- | --- | --- |
| 항상 앞 120자만 나옴 | `AI_PROVIDER`가 mock이다 | 외부 주소가 있을 때만 http로 바꾼다 |
| 스트림이 401 | `/api/ai/stream`은 인증이 필요하다 | Bearer 토큰을 붙인다 |
| MCP가 질문을 못 찾음 | 학습용 파서가 `query` 문자열만 본다 | JSON을 한 줄로 보내고 따옴표를 맞춘다 |
| 모델이 문서 밖을 말함 | 원격 프롬프트가 약하다 | 프롬프트에 발췌 밖은 답하지 말라고 적고, mock 검색을 기본으로 둔다 |

## ⑥ 직접 해보기

1. `/tools`에서 문서에 있는 단어와 없는 단어를 질문한다.
2. `McpStudyServerTest`를 읽어 `찾을 수 없습니다` 검증을 찾는다.
3. `AI_API_KEY`를 로그나 응답에 찍는 코드가 없는지 `SummaryClient`에서 확인한다.

## ⑦ ☐ 체크리스트

- [ ] 요약과 질의응답이 문서 문장만 근거로 동작한다
- [ ] 스트리밍 응답은 SSE 경로로 받을 수 있다
- [ ] MCP `search_document`가 한 줄 JSON으로 응답한다
- [ ] DocuForge의 업로드·요약·근거 답변과 대응 관계를 말할 수 있다
- [ ] 모델 키는 환경변수에만 있다

## ⑧ 다음 단계

[studyFinal](studyFinal.md)에서 DB 교체, 외부 서비스, AI, 배포, 테스트를 한 서비스 점검으로 모은다.

참고: [DocuForge 노트](https://app.notion.com/p/40bd7dec5e2e818d9c91c6fb26dccc4c)
