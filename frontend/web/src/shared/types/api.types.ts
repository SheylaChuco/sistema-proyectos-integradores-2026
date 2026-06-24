export interface ApiResponse<T> {
  success: boolean;
  data: T;
}

export interface ApiError {
  code: string;
  message: string;
}

export interface ApiErrorResponse {
  success: false;
  error: ApiError;
}

export interface PaginaDto<T> {
  content: T[];
  page: number;
  size: number;
  total_elements: number;
  total_pages: number;
}
