import axios from 'axios';

const API_BASE_URL = process.env.NEXT_PUBLIC_API_BASE_URL || '/api/meta';
const AUTH_API_URL = process.env.NEXT_PUBLIC_AUTH_API_URL || '/api/auth';
const CONTENT_API_URL = process.env.NEXT_PUBLIC_CONTENT_API_URL || '/api/content';

const apiClient = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

const authClient = axios.create({
  baseURL: AUTH_API_URL,
  withCredentials: true,
});

const contentClient = axios.create({
  baseURL: CONTENT_API_URL,
  withCredentials: true,
});

export interface Film {
  filmId: string;
  name: string;
  description: string;
  available: boolean;
  createdAt: string;
  updatedAt: string;
  durationSeconds?: number;
}

export interface UserInfo {
  username: string;
  email: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface LoginRequest {
  username: string;
  password: string;
}

export const authApi = {
  register: async (data: RegisterRequest) => {
    const response = await authClient.post('/register', data);
    return response.data;
  },
  login: async (data: LoginRequest) => {
    const response = await authClient.post('/login', data);
    return response.data;
  },
  logout: async () => {
    try {
      await authClient.post('/logout');
    } catch (error) {
      console.error('Logout API error:', error);
    }
  },
  getCurrentUser: async (): Promise<UserInfo> => {
    const response = await authClient.get('/me');
    return response.data;
  },
};

export const filmsApi = {
  getAllFilms: async (): Promise<Film[]> => {
    const response = await apiClient.get('/films');
    return response.data;
  },
  getAvailableFilms: async (): Promise<Film[]> => {
    const response = await apiClient.get('/films/available');
    return response.data;
  },
  getFilm: async (filmId: string): Promise<Film> => {
    const response = await apiClient.get(`/films/${filmId}`);
    return response.data;
  },
  checkAvailability: async (filmId: string): Promise<{ available: boolean }> => {
    const response = await apiClient.get(`/films/${filmId}/availability`);
    return response.data;
  },
};

export const videoApi = {
  getStreamUrl: (filmId: string, quality?: string) => {
    const params = quality ? `?quality=${quality}` : '';
    return `${CONTENT_API_URL}/videos/${filmId}/stream${params}`;
  },
  getAvailableQualities: async (filmId: string): Promise<string[]> => {
    const response = await contentClient.get(`/videos/${filmId}/qualities`);
    return response.data;
  },
};
