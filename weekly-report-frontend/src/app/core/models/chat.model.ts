export interface ChatRequest {
  question: string;
}

export interface ChatResponse {
  answer: string;
}

export interface TeamSummaryResponse {
  weekStartDate: string;
  summary: string;
}

export interface ChatTurn {
  role: 'user' | 'assistant';
  text: string;
}
