'use client';

import { useRouter } from 'next/navigation';
import { Card, CardContent, CardActions, Typography, Button } from '@mui/material';
import { Film } from '@/lib/api';
import { formatDuration } from '@/utils/formatDuration';

interface FilmCardProps {
  film: Film;
}

export default function FilmCard({ film }: FilmCardProps) {
  const router = useRouter();

  return (
    <Card sx={{ display: 'flex', flexDirection: 'column', height: '100%' }}>
      <CardContent sx={{ flexGrow: 1 }}>
        <Typography variant="h6" component="h2" gutterBottom>
          {film.name}
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ 
          overflow: 'hidden',
          textOverflow: 'ellipsis',
          display: '-webkit-box',
          WebkitLineClamp: 3,
          WebkitBoxOrient: 'vertical',
        }}>
          {film.description || 'No description available'}
        </Typography>
        {film.durationSeconds !== undefined && film.durationSeconds !== null && film.durationSeconds > 0 && (
          <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
            Duration: {formatDuration(film.durationSeconds)}
          </Typography>
        )}
      </CardContent>
      <CardActions>
        <Button size="small" onClick={() => router.push(`/films/${film.filmId}`)}>
          Watch
        </Button>
      </CardActions>
    </Card>
  );
}

