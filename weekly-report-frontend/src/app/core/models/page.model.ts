/** Mirrors the JSON shape Spring Data's Page<T> serializes to. */
export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;   // current page index, 0-based
  size: number;
  first: boolean;
  last: boolean;
}
