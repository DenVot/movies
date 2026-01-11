'use client';

import { useEffect, useState, useRef } from 'react';
import { useParams, useRouter } from 'next/navigation';
import { Container, Box, Typography, Button, Select, MenuItem, FormControl, InputLabel, Alert } from '@mui/material';
import { useFilmsStore } from '@/store/filmsStore';
import { videoApi } from '@/lib/api';
import { formatDuration } from '@/utils/formatDuration';

export default function FilmWatchPage() {
  const params = useParams();
  const router = useRouter();
  const filmId = params.filmId as string;
  const { selectedFilm, fetchFilm, isLoading } = useFilmsStore();
  const [selectedQuality, setSelectedQuality] = useState<string>('');
  const [qualities, setQualities] = useState<string[]>([]);
  const [loadingQualities, setLoadingQualities] = useState(false);
  const videoRef = useRef<HTMLVideoElement>(null);
  const currentTimeRef = useRef<number>(0);
  const wasPlayingRef = useRef<boolean>(false);

  useEffect(() => {
    if (filmId) {
      fetchFilm(filmId);
      loadQualities();
    }
  }, [filmId, fetchFilm]);

  const loadQualities = async () => {
    setLoadingQualities(true);
    try {
      const availableQualities = await videoApi.getAvailableQualities(filmId);
      setQualities(availableQualities);
      if (availableQualities.length > 0) {
        setSelectedQuality(availableQualities[0]);
      }
    } catch (error) {
      console.error('Failed to load qualities:', error);
    } finally {
      setLoadingQualities(false);
    }
  };

  useEffect(() => {
    const video = videoRef.current;
    if (!video) return;

    const handleTimeUpdate = () => {
      currentTimeRef.current = video.currentTime;
    };

    video.addEventListener('timeupdate', handleTimeUpdate);
    return () => {
      video.removeEventListener('timeupdate', handleTimeUpdate);
    };
  }, []);

  useEffect(() => {
    const video = videoRef.current;
    if (!video || currentTimeRef.current === 0) return;

    const handleCanPlay = () => {
      video.currentTime = currentTimeRef.current;
      if (wasPlayingRef.current) {
        video.play().catch(() => {
        });
      }
    };

    video.addEventListener('canplay', handleCanPlay, { once: true });
    return () => {
      video.removeEventListener('canplay', handleCanPlay);
    };
  }, [selectedQuality]);

  const handleQualityChange = (newQuality: string) => {
    if (videoRef.current) {
      currentTimeRef.current = videoRef.current.currentTime;
      wasPlayingRef.current = !videoRef.current.paused;
    }
    setSelectedQuality(newQuality);
  };

  const videoUrl = selectedQuality ? videoApi.getStreamUrl(filmId, selectedQuality) : videoApi.getStreamUrl(filmId);

  if (isLoading) {
    return (
      <Container>
        <Box sx={{ display: 'flex', justifyContent: 'center', alignItems: 'center', minHeight: '100vh' }}>
          <Typography>Loading...</Typography>
        </Box>
      </Container>
    );
  }

  if (!selectedFilm) {
    return (
      <Container>
        <Box sx={{ mt: 4 }}>
          <Alert severity="error">Film not found</Alert>
          <Button onClick={() => router.push('/')} sx={{ mt: 2 }}>
            Back to Home
          </Button>
        </Box>
      </Container>
    );
  }

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Button onClick={() => router.push('/')} sx={{ mb: 2 }}>
        Back to Home
      </Button>
      <Typography variant="h4" component="h1" gutterBottom>
        {selectedFilm.name}
      </Typography>
      <Typography variant="body1" color="text.secondary" paragraph>
        {selectedFilm.description}
      </Typography>
      {selectedFilm.durationSeconds !== undefined && selectedFilm.durationSeconds !== null && selectedFilm.durationSeconds > 0 && (
        <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
          Duration: {formatDuration(selectedFilm.durationSeconds)}
        </Typography>
      )}

      {qualities.length > 0 && (
        <FormControl sx={{ minWidth: 200, mb: 2 }}>
          <InputLabel>Quality</InputLabel>
          <Select
            value={selectedQuality}
            label="Quality"
            onChange={(e) => handleQualityChange(e.target.value)}
            disabled={loadingQualities}
          >
            {qualities.map((quality) => (
              <MenuItem key={quality} value={quality}>
                {quality}
              </MenuItem>
            ))}
          </Select>
        </FormControl>
      )}

      <Box sx={{ mt: 3, position: 'relative', paddingTop: '56.25%', backgroundColor: '#000' }}>
        <video
          ref={videoRef}
          controls
          style={{
            position: 'absolute',
            top: 0,
            left: 0,
            width: '100%',
            height: '100%',
          }}
          src={videoUrl}
        >
          Your browser does not support the video tag.
        </video>
      </Box>
    </Container>
  );
}

