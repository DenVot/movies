'use client';

import { useState, useEffect } from 'react';
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
} from '@mui/material';
import { useFilmsStore } from '@/store/filmsStore';

interface FilmEditDialogProps {
  open: boolean;
  onClose: () => void;
  filmId: string | null;
  mode: 'create' | 'edit';
}

export default function FilmEditDialog({ open, onClose, filmId, mode }: FilmEditDialogProps) {
  const { selectedFilm, fetchFilm, createFilm, updateFilm, setSelectedFilm } = useFilmsStore();
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (open && mode === 'edit' && filmId) {
      fetchFilm(filmId);
    } else if (open && mode === 'create') {
      setName('');
      setDescription('');
      setSelectedFilm(null);
    }
  }, [open, filmId, mode, fetchFilm, setSelectedFilm]);

  useEffect(() => {
    if (open && selectedFilm && mode === 'edit' && filmId === selectedFilm.filmId) {
      setName(selectedFilm.name);
      setDescription(selectedFilm.description || '');
    }
  }, [selectedFilm, mode, open, filmId]);

  const handleSubmit = async () => {
    setLoading(true);
    try {
      if (mode === 'create') {
        await createFilm({ name, description });
      } else if (filmId) {
        await updateFilm(filmId, { name, description });
      }
      onClose();
    } catch (error) {
      console.error('Failed to save film:', error);
    } finally {
      setLoading(false);
    }
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{mode === 'create' ? 'Create Film' : 'Edit Film'}</DialogTitle>
      <DialogContent>
        <TextField
          autoFocus
          margin="dense"
          label="Name"
          fullWidth
          variant="outlined"
          value={name}
          onChange={(e) => setName(e.target.value)}
          sx={{ mb: 2 }}
        />
        <TextField
          margin="dense"
          label="Description"
          fullWidth
          multiline
          rows={4}
          variant="outlined"
          value={description}
          onChange={(e) => setDescription(e.target.value)}
        />
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Cancel</Button>
        <Button onClick={handleSubmit} variant="contained" disabled={loading || !name}>
          {loading ? 'Saving...' : 'Save'}
        </Button>
      </DialogActions>
    </Dialog>
  );
}

