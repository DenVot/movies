import { create } from 'zustand';
import { filmsApi, Film, CreateFilmRequest, UpdateFilmRequest } from '@/lib/api';

interface FilmsState {
  films: Film[];
  selectedFilm: Film | null;
  isLoading: boolean;
  error: string | null;
  fetchFilms: () => Promise<void>;
  fetchFilm: (filmId: string) => Promise<void>;
  createFilm: (data: CreateFilmRequest) => Promise<void>;
  updateFilm: (filmId: string, data: UpdateFilmRequest) => Promise<void>;
  deleteFilm: (filmId: string) => Promise<void>;
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
  fetchFilm: async (filmId: string) => {
    set({ isLoading: true, error: null });
    try {
      const film = await filmsApi.getFilm(filmId);
      set({ selectedFilm: film, isLoading: false });
    } catch (error: any) {
      set({ error: error.message || 'Failed to fetch film', isLoading: false });
    }
  },
  createFilm: async (data: CreateFilmRequest) => {
    set({ isLoading: true, error: null });
    try {
      await filmsApi.createFilm(data);
      await filmsApi.getAllFilms().then((films) => set({ films }));
      set({ isLoading: false });
    } catch (error: any) {
      set({ error: error.message || 'Failed to create film', isLoading: false });
      throw error;
    }
  },
  updateFilm: async (filmId: string, data: UpdateFilmRequest) => {
    set({ isLoading: true, error: null });
    try {
      await filmsApi.updateFilm(filmId, data);
      await filmsApi.getAllFilms().then((films) => set({ films }));
      set({ isLoading: false });
    } catch (error: any) {
      set({ error: error.message || 'Failed to update film', isLoading: false });
      throw error;
    }
  },
  deleteFilm: async (filmId: string) => {
    set({ isLoading: true, error: null });
    try {
      await filmsApi.deleteFilm(filmId);
      await filmsApi.getAllFilms().then((films) => set({ films }));
      set({ isLoading: false });
    } catch (error: any) {
      set({ error: error.message || 'Failed to delete film', isLoading: false });
      throw error;
    }
  },
  setSelectedFilm: (film: Film | null) => {
    set({ selectedFilm: film });
  },
}));

