import { create } from 'zustand';
import { filmsApi, Film } from '@/lib/api';

interface FilmsState {
  films: Film[];
  selectedFilm: Film | null;
  isLoading: boolean;
  error: string | null;
  fetchFilms: () => Promise<void>;
  fetchAvailableFilms: () => Promise<void>;
  fetchFilm: (filmId: string) => Promise<void>;
  setSelectedFilm: (film: Film | null) => void;
}

export const useFilmsStore = create<FilmsState>((set) => ({
  films: [],
  selectedFilm: null,
  isLoading: false,
  error: null,
  fetchFilms: async () => {
    set({ isLoading: true, error: null });
    try {
      const films = await filmsApi.getAllFilms();
      set({ films, isLoading: false });
    } catch (error: any) {
      set({ error: error.message || 'Failed to fetch films', isLoading: false });
    }
  },
  fetchAvailableFilms: async () => {
    set({ isLoading: true, error: null });
    try {
      const films = await filmsApi.getAvailableFilms();
      set({ films, isLoading: false });
    } catch (error: any) {
      set({ error: error.message || 'Failed to fetch available films', isLoading: false });
    }
  },
  fetchFilm: async (filmId: string) => {
    set({ isLoading: true, error: null });
    try {
      const film = await filmsApi.getFilm(filmId);
      set({ selectedFilm: film, isLoading: false });
    } catch (error: any) {
      set({ error: error.message || 'Failed to fetch film', isLoading: false });
    }
  },
  setSelectedFilm: (film: Film | null) => {
    set({ selectedFilm: film });
  },
}));

