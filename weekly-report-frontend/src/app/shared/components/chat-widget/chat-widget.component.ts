import { Component, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { ChatService } from '../../../core/services/chat.service';
import { ChatTurn } from '../../../core/models/chat.model';

/**
 * Simple in-app chat widget (assignment section 8, "Good to have").
 * Each question is answered independently by the backend's tool-use loop - there's no
 * shared conversation memory server-side - so this keeps a visible log of turns for the
 * manager's own context, without implying the assistant remembers earlier questions.
 */
@Component({
  selector: 'app-chat-widget',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './chat-widget.component.html',
  styleUrl: './chat-widget.component.scss'
})
export class ChatWidgetComponent {
  open = signal(false);
  turns = signal<ChatTurn[]>([]);
  question = '';
  asking = signal(false);
  errorMessage = signal<string | null>(null);
  unavailable = signal(false);

  constructor(private readonly chatService: ChatService) {}

  toggle(): void {
    this.open.set(!this.open());
  }

  ask(): void {
    const question = this.question.trim();
    if (!question || this.asking()) return;

    this.turns.update((t) => [...t, { role: 'user', text: question }]);
    this.question = '';
    this.asking.set(true);
    this.errorMessage.set(null);

    this.chatService.ask(question).subscribe({
      next: (res) => {
        this.turns.update((t) => [...t, { role: 'assistant', text: res.answer }]);
        this.asking.set(false);
      },
      error: (err) => {
        this.asking.set(false);
        if (err.status === 503) {
          this.unavailable.set(true);
        } else {
          this.errorMessage.set(err?.error?.message ?? 'Could not reach the assistant.');
        }
      }
    });
  }
}
