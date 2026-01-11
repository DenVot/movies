import axios from 'axios';

const ADMIN_API_URL = process.env.NEXT_PUBLIC_ADMIN_API_URL || '/api/admin';
const AUTH_API_URL = process.env.NEXT_PUBLIC_AUTH_API_URL || '/api/auth';

const adminClient = axios.create({
  baseURL: ADMIN_API_URL,
  withCredentials: true,
});

const authClient = axios.create({
  baseURL: AUTH_API_URL,
  withCredentials: true,
});

export interface Film {
  filmId: string;
  name: string;
  description: string;
  available: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface UserInfo {
  username: string;
  email: string;
}

export interface CreateFilmRequest {
  name: string;
  description: string;
}

export interface UpdateFilmRequest {
  name: string;
  description: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export const authApi = {
  login: async (data: LoginRequest) => {
    const response = await authClient.post('/login', data);
    return response.data;
  },
  getCurrentUser: async (): Promise<UserInfo> => {
    const response = await authClient.get('/me');
    return response.data;
  },
};

export const filmsApi = {
  getAllFilms: async (): Promise<Film[]> => {
    const response = await adminClient.get('/films');
    return response.data;
  },
  getFilm: async (filmId: string): Promise<Film> => {
    const response = await adminClient.get(`/films/${filmId}`);
    return response.data;
  },
  createFilm: async (data: CreateFilmRequest): Promise<{ filmId: string }> => {
    const response = await adminClient.post('/films', data);
    return response.data;
  },
  updateFilm: async (filmId: string, data: UpdateFilmRequest): Promise<void> => {
    await adminClient.put(`/films/${filmId}`, data);
  },
  deleteFilm: async (filmId: string): Promise<void> => {
    await adminClient.delete(`/films/${filmId}`);
  },
  uploadVideo: async (filmId: string, file: File): Promise<void> => {
    const formData = new FormData();
    formData.append('file', file);
    await adminClient.post(`/films/${filmId}/video`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
  },
};
