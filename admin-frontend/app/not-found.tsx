'use client';

import { useRouter } from 'next/navigation';
import { Container, Box, Typography, Button } from '@mui/material';
import { Home } from '@mui/icons-material';

export default function NotFound() {
  const router = useRouter();

  return (
    <Container>
      <Box
        sx={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          justifyContent: 'center',
          minHeight: '100vh',
          textAlign: 'center',
          gap: 3,
        }}
      >
        <Typography variant="h1" component="h1" sx={{ fontSize: { xs: '4rem', sm: '6rem' }, fontWeight: 'bold' }}>
          404
        </Typography>
        <Typography variant="h4" component="h2" gutterBottom>
          Страница не найдена
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ maxWidth: '500px' }}>
          Страница, которую вы ищете, не существует или была перемещена.
        </Typography>
        <Button
          variant="contained"
          size="large"
          startIcon={<Home />}
          onClick={() => router.push('/')}
          sx={{ mt: 2 }}
        >
          На главную
        </Button>
      </Box>
    </Container>
  );
}

