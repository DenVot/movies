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
          Page Not Found
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ maxWidth: '500px' }}>
          The page you are looking for does not exist or has been moved.
        </Typography>
        <Button
          variant="contained"
          size="large"
          startIcon={<Home />}
          onClick={() => router.push('/')}
          sx={{ mt: 2 }}
        >
          Go to Home Page
        </Button>
      </Box>
    </Container>
  );
}

