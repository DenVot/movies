'use client';

import { useState } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Alert,
  Box,
  Typography,
} from '@mui/material';
import { filmsApi } from '@/lib/api';

interface VideoUploadDialogProps {
  open: boolean;
  onClose: () => void;
  filmId: string | null;
}

export default function VideoUploadDialog({ open, onClose, filmId }: VideoUploadDialogProps) {
  const [file, setFile] = useState<File | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);

  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files && e.target.files[0]) {
      const selectedFile = e.target.files[0];
      if (selectedFile.type !== 'video/mp4') {
        setError('Поддерживаются только файлы MP4');
        return;
      }
      setFile(selectedFile);
      setError('');
    }
  };

  const handleSubmit = async () => {
    if (!file || !filmId) return;

    setLoading(true);
    setError('');
    setSuccess(false);

    try {
      await filmsApi.uploadVideo(filmId, file);
      setSuccess(true);
      setTimeout(() => {
        onClose();
        setFile(null);
        setSuccess(false);
      }, 2000);
    } catch (err: any) {
      setError(err.response?.data?.message || 'Ошибка загрузки видео');
    } finally {
      setLoading(false);
    }
  };

  const handleClose = () => {
    setFile(null);
    setError('');
    setSuccess(false);
    onClose();
  };

  return (
    <Dialog open={open} onClose={handleClose} maxWidth="sm" fullWidth>
      <DialogTitle>Загрузить видео</DialogTitle>
      <DialogContent>
        {success && <Alert severity="success" sx={{ mb: 2 }}>Видео успешно загружено!</Alert>}
        {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
        <Box sx={{ mt: 1 }}>
          <input
            type="file"
            accept="video/mp4"
            onChange={handleFileChange}
            style={{ marginBottom: '8px' }}
          />
          {file && (
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              Размер файла: {(file.size / 1024 / 1024).toFixed(2)} МБ
            </Typography>
          )}
        </Box>
      </DialogContent>
      <DialogActions>
        <Button onClick={handleClose}>Отмена</Button>
        <Button onClick={handleSubmit} variant="contained" disabled={loading || !file}>
          {loading ? 'Загрузка...' : 'Загрузить'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

