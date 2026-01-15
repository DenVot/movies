'use client';

import { useEffect } from 'react';
import { useRouter } from 'next/navigation';
import { Container, Box, Typography } from '@mui/material';
import { useAuthStore } from '@/store/authStore';
import FilmsList from '@/components/FilmsList';

export default function AdminHome() {
  const router = useRouter();
  const { isAuthenticated, isLoading, checkAuth } = useAuthStore();

  useEffect(() => {
    checkAuth();
  }, [checkAuth]);

  useEffect(() => {
    if (!isLoading && !isAuthenticated) {
      router.push('/login');
    }
  }, [isAuthenticated, isLoading, router]);

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
    return null;
  }

  return (
    <Container maxWidth="xl" sx={{ py: 4 }}>
      <Typography variant="h4" component="h1" gutterBottom>
        Управление фильмами
      </Typography>
      <FilmsList />
    </Container>
  );
}
