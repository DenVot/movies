'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Box, Button, Container, Typography } from '@mui/material';
import { useAuthStore } from '@/store/authStore';
import { useFilmsStore } from '@/store/filmsStore';
import FilmCard from '@/components/FilmCard';

export default function Home() {
  const router = useRouter();
  const { isAuthenticated, isLoading, checkAuth } = useAuthStore();
  const { films, fetchAvailableFilms, isLoading: filmsLoading } = useFilmsStore();

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  useEffect(() => {
    if (isAuthenticated) {
      fetchAvailableFilms();
    }
  }, [isAuthenticated, fetchAvailableFilms]);

  if (isLoading) {
    return (
      <Container>
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
          <Typography>Загрузка...</Typography>
        </Box>
      </Container>
    );
  }

  if (!isAuthenticated) {
    return (
      <Container>
        <Box sx={{ display: 'flex', flexDirection: 'column', alignItems: 'center', justifyContent: 'center', minHeight: '100vh', gap: 2 }}>
          <Typography variant="h3" component="h1" gutterBottom>
            Добро пожаловать в Онлайн Кинотеатр
          </Typography>
          <Typography variant="body1" color="text.secondary" gutterBottom>
            Пожалуйста, войдите или зарегистрируйтесь, чтобы смотреть фильмы
          </Typography>
          <Box sx={{ display: 'flex', gap: 2 }}>
            <Button variant="contained" onClick={() => router.push('/login')}>
              Войти
            </Button>
            <Button variant="outlined" onClick={() => router.push('/register')}>
              Зарегистрироваться
            </Button>
          </Box>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Доступные фильмы
      </Typography>
      {filmsLoading ? (
        <Typography>Загрузка фильмов...</Typography>
      ) : films.length === 0 ? (
        <Typography>Нет доступных фильмов</Typography>
      ) : (
        <Box sx={{ display: 'grid', gridTemplateColumns: { xs: '1fr', sm: 'repeat(2, 1fr)', md: 'repeat(3, 1fr)', lg: 'repeat(4, 1fr)' }, gap: 3, mt: 3 }}>
          {films.map((film) => (
            <FilmCard key={film.filmId} film={film} />
          ))}
        </Box>
      )}
    </Container>
  );
}
