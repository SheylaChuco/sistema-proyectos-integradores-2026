import { useState, useRef, useEffect } from 'react';
import ReactMarkdown from 'react-markdown';
import { enviarMensaje, type MensajeChat } from '../services/asistenteService';
import type { ApiErrorResponse } from '../../../shared/types/api.types';

const VENTANA = 10;

const MSG_BIENVENIDA: MensajeChat = {
  role: 'assistant',
  content: 'Hola, soy el Asistente IA de Integratec. Puedo ayudarte con consultas sobre proyectos históricos y nuevos de TECSUP. ¿En qué puedo ayudarte?',
};

export default function AsistenteIA() {
  const [historial, setHistorial] = useState<MensajeChat[]>([MSG_BIENVENIDA]);
  const [input, setInput] = useState('');
  const [cargando, setCargando] = useState(false);
  const endRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [historial, cargando]);

  async function handleEnviar(e: React.FormEvent) {
    e.preventDefault();
    const texto = input.trim();
    if (!texto || cargando) return;

    const nuevoHistorial: MensajeChat[] = [...historial, { role: 'user', content: texto }];
    setHistorial(nuevoHistorial);
    setInput('');
    setCargando(true);

    try {
      const ventana = nuevoHistorial.slice(-VENTANA);
      const respuesta = await enviarMensaje({ mensaje: texto, historial: ventana.slice(0, -1) });
      setHistorial((h) => [...h, { role: 'assistant', content: respuesta }]);
    } catch (e) {
      const err = e as ApiErrorResponse;
      setHistorial((h) => [
        ...h,
        { role: 'assistant', content: err.error?.message ?? 'Ocurrió un error. Por favor intenta nuevamente.' },
      ]);
    } finally {
      setCargando(false);
      setTimeout(() => inputRef.current?.focus(), 50);
    }
  }

  return (
    <div className="flex flex-col h-full">
      {/* Header */}
      <div className="bg-white border-b border-[#E2E8F0] px-6 py-3 flex items-center gap-3">
        <div className="w-9 h-9 rounded-full bg-[#6C63FF] flex items-center justify-center flex-shrink-0">
          <svg className="w-5 h-5 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
        </div>
        <div>
          <p className="text-sm font-semibold text-[#0F172A]">Asistente IA</p>
          <div className="flex items-center gap-1.5">
            <div className="w-1.5 h-1.5 rounded-full bg-green-500" />
            <p className="text-xs text-[#2563EB]">En línea · Integratec</p>
          </div>
        </div>
        <div className="ml-auto w-8 h-8 rounded-full bg-[#EEF2F7] flex items-center justify-center">
          <span className="text-xs font-bold text-[#64748B]">IA</span>
        </div>
      </div>

      {/* Mensajes */}
      <div className="flex-1 overflow-y-auto px-6 py-5 space-y-4 bg-white">
        {historial.map((msg, i) => (
          <div key={i} className={`flex ${msg.role === 'user' ? 'justify-end' : 'justify-start'} items-start gap-3`}>
            {msg.role === 'assistant' && (
              <div className="w-8 h-8 rounded-full bg-[#6C63FF] flex items-center justify-center flex-shrink-0 mt-0.5">
                <svg className="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
                  <path strokeLinecap="round" strokeLinejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
                </svg>
              </div>
            )}
            <div
              className={`max-w-[72%] px-4 py-3 rounded-2xl text-sm leading-relaxed ${
                msg.role === 'user'
                  ? 'bg-[#2563EB] text-white rounded-tr-sm'
                  : 'bg-[#F8FAFC] text-[#374151] border border-[#E2E8F0] rounded-tl-sm'
              }`}
            >
              {msg.role === 'assistant' ? (
                <ReactMarkdown
                  components={{
                    p: ({ children }) => <p className="mb-2 last:mb-0">{children}</p>,
                    ul: ({ children }) => <ul className="list-disc list-inside space-y-1 mb-2">{children}</ul>,
                    ol: ({ children }) => <ol className="list-decimal list-inside space-y-1 mb-2">{children}</ol>,
                    li: ({ children }) => <li className="leading-snug">{children}</li>,
                    strong: ({ children }) => <strong className="font-semibold text-[#0F172A]">{children}</strong>,
                    h3: ({ children }) => <p className="font-semibold text-[#0F172A] mt-2 mb-1">{children}</p>,
                    h4: ({ children }) => <p className="font-medium text-[#374151] mt-1.5 mb-1">{children}</p>,
                  }}
                >
                  {msg.content}
                </ReactMarkdown>
              ) : (
                msg.content
              )}
            </div>
          </div>
        ))}

        {cargando && (
          <div className="flex items-start gap-3">
            <div className="w-8 h-8 rounded-full bg-[#6C63FF] flex items-center justify-center flex-shrink-0">
              <svg className="w-4 h-4 text-white" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={1.8}>
                <path strokeLinecap="round" strokeLinejoin="round" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
              </svg>
            </div>
            <div className="bg-[#F8FAFC] border border-[#E2E8F0] rounded-2xl rounded-tl-sm px-4 py-3">
              <div className="flex gap-1 items-center h-4">
                <span className="w-1.5 h-1.5 rounded-full bg-[#6C63FF] animate-bounce [animation-delay:0ms]" />
                <span className="w-1.5 h-1.5 rounded-full bg-[#6C63FF] animate-bounce [animation-delay:150ms]" />
                <span className="w-1.5 h-1.5 rounded-full bg-[#6C63FF] animate-bounce [animation-delay:300ms]" />
              </div>
            </div>
          </div>
        )}
        <div ref={endRef} />
      </div>

      {/* Input */}
      <div className="bg-white border-t border-[#E2E8F0] px-6 py-4">
        <form onSubmit={handleEnviar} className="flex gap-3">
          <input
            ref={inputRef}
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            placeholder="Pregunta sobre proyectos de TECSUP..."
            disabled={cargando}
            className="flex-1 px-4 py-2.5 rounded-xl border border-[#CBD5E1] text-sm placeholder-[#94A3B8] focus:outline-none focus:ring-2 focus:ring-[#6C63FF] focus:border-transparent disabled:opacity-60 transition"
          />
          <button
            type="submit"
            disabled={cargando || !input.trim()}
            className="px-5 py-2.5 bg-[#2563EB] hover:bg-[#1D4ED8] text-white font-semibold text-sm rounded-xl transition disabled:opacity-50 disabled:cursor-not-allowed"
          >
            Enviar
          </button>
        </form>
      </div>
    </div>
  );
}
