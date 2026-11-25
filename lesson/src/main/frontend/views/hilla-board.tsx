import { useEffect, useState } from "react";
import { PostBrowserService } from "Frontend/generated/endpoints";

type Author = { id: number; nickname: string };
type PostCard = {
  id: number;
  title: string;
  author: Author;
  commentCount: number;
  createdAt: string;
};

export default function HillaBoardView() {
  const [rows, setRows] = useState<PostCard[]>([]);
  const [message, setMessage] = useState("불러오는 중");

  useEffect(() => {
    PostBrowserService.list("", 1, 10)
      .then((data) => {
        const next = (data ?? []).filter((row): row is PostCard => Boolean(row));
        setRows(next);
        setMessage(next.length === 0 ? "글이 없습니다." : "");
      })
      .catch(() => setMessage("목록을 불러오지 못했습니다."));
  }, []);

  return (
    <main style={{ padding: "1.5rem", fontFamily: "sans-serif" }}>
      <h1>Hilla 게시판</h1>
      <p>이 화면의 필드는 GET /api/posts 목록과 같다. id, title, author, commentCount, createdAt.</p>
      {message && <p role="alert">{message}</p>}
      <ul>
        {rows.map((row) => (
          <li key={row.id}>
            {row.title} · {row.author.nickname} · 댓글 {row.commentCount}
          </li>
        ))}
      </ul>
    </main>
  );
}
