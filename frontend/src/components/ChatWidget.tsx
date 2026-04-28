import { useState, useRef, useEffect } from 'react';
import { chatApi } from '../api/chat';

interface Message {
  role: 'user' | 'ai';
  text: string;
}

interface Props {
  bookingNumber: string;
}

export default function ChatWidget({ bookingNumber }: Props) {
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState<Message[]>([
    { role: 'ai', text: '안녕하세요! 예약에 관해 궁금한 점이 있으시면 질문해주세요.' },
  ]);
  const [input, setInput] = useState('');
  const [loading, setLoading] = useState(false);
  const bottomRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    bottomRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  const send = async () => {
    const text = input.trim();
    if (!text || loading) return;

    setMessages((prev) => [...prev, { role: 'user', text }]);
    setInput('');
    setLoading(true);

    try {
      const res = await chatApi.ask(bookingNumber, text);
      setMessages((prev) => [...prev, { role: 'ai', text: res.data.reply }]);
    } catch {
      setMessages((prev) => [...prev, { role: 'ai', text: '죄송합니다. 일시적인 오류가 발생했습니다.' }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed bottom-6 right-6 z-50">
      {open && (
        <div className="mb-3 w-80 bg-white rounded-2xl shadow-2xl border border-slate-200 flex flex-col overflow-hidden">
          <div className="bg-navio-primary text-white px-4 py-3 flex justify-between items-center">
            <span className="font-semibold text-sm">✈️ AI 여행 도우미</span>
            <button onClick={() => setOpen(false)} className="text-white/80 hover:text-white text-lg leading-none">×</button>
          </div>

          <div className="flex-1 h-72 overflow-y-auto p-3 space-y-3">
            {messages.map((m, i) => (
              <div key={i} className={`flex ${m.role === 'user' ? 'justify-end' : 'justify-start'}`}>
                <div
                  className={`max-w-[85%] px-3 py-2 rounded-xl text-sm leading-relaxed whitespace-pre-wrap ${
                    m.role === 'user'
                      ? 'bg-navio-primary text-white rounded-br-none'
                      : 'bg-slate-100 text-slate-800 rounded-bl-none'
                  }`}
                >
                  {m.text}
                </div>
              </div>
            ))}
            {loading && (
              <div className="flex justify-start">
                <div className="bg-slate-100 text-slate-500 px-3 py-2 rounded-xl text-sm rounded-bl-none">
                  답변 생성 중...
                </div>
              </div>
            )}
            <div ref={bottomRef} />
          </div>

          <div className="border-t border-slate-100 p-3 flex gap-2">
            <input
              value={input}
              onChange={(e) => setInput(e.target.value)}
              onKeyDown={(e) => e.key === 'Enter' && !e.shiftKey && send()}
              placeholder="질문을 입력하세요..."
              className="flex-1 text-sm border border-slate-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-1 focus:ring-navio-primary"
            />
            <button
              onClick={send}
              disabled={loading || !input.trim()}
              className="bg-navio-primary text-white px-3 py-2 rounded-lg text-sm font-medium hover:opacity-90 disabled:opacity-50"
            >
              전송
            </button>
          </div>
        </div>
      )}

      <button
        onClick={() => setOpen((v) => !v)}
        className="w-14 h-14 bg-navio-primary text-white rounded-full shadow-lg flex items-center justify-center text-2xl hover:opacity-90 transition-opacity"
        title="AI 여행 도우미"
      >
        {open ? '×' : '💬'}
      </button>
    </div>
  );
}
