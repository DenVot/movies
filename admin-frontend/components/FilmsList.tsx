'use client';

import { useEffect, useState } from 'react';
import {
  Box,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  IconButton,
  Typography,
  Alert,
  CircularProgress,
} from '@mui/material';
import EditIcon from '@mui/icons-material/Edit';
import DeleteIcon from '@mui/icons-material/Delete';
import AddIcon from '@mui/icons-material/Add';
import VideoLibraryIcon from '@mui/icons-material/VideoLibrary';
import { useFilmsStore } from '@/store/filmsStore';
import FilmEditDialog from './FilmEditDialog';
import VideoUploadDialog from './VideoUploadDialog';

export default function FilmsList() {
  const { films, fetchFilms, deleteFilm, isLoading, error } = useFilmsStore();
  const [editDialogOpen, setEditDialogOpen] = useState(false);
  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [uploadDialogOpen, setUploadDialogOpen] = useState(false);
  const [selectedFilmId, setSelectedFilmId] = useState<string | null>(null);

  useEffect(() => {
    fetchFilms();
  }, [fetchFilms]);

  const handleDelete = async (filmId: string) => {
    if (window.confirm('Вы уверены, что хотите удалить этот фильм?')) {
      try {
        await deleteFilm(filmId);
      } catch (error) {
        console.error('Failed to delete film:', error);
      }
    }
  };

  const handleEdit = (filmId: string) => {
    setSelectedFilmId(filmId);
    setEditDialogOpen(true);
  };

  const handleUpload = (filmId: string) => {
    setSelectedFilmId(filmId);
    setUploadDialogOpen(true);
  };

  if (isLoading && films.length === 0) {
    return (
      <Box sx={{ display: 'flex', justifyContent: 'center', p: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Box sx={{ display: 'flex', justifyContent: 'flex-end', mb: 2 }}>
        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={() => setCreateDialogOpen(true)}
        >
          Добавить фильм
        </Button>
      </Box>
      {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
      <TableContainer component={Paper}>
        <Table>
          <TableHead>
            <TableRow>
              <TableCell>Название</TableCell>
              <TableCell>Описание</TableCell>
              <TableCell>Доступен</TableCell>
              <TableCell>Дата создания</TableCell>
              <TableCell align="right">Действия</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {films.map((film) => (
              <TableRow key={film.filmId}>
                <TableCell>{film.name}</TableCell>
                <TableCell>{film.description || '-'}</TableCell>
                <TableCell>{film.available ? 'Да' : 'Нет'}</TableCell>
                <TableCell>{new Date(film.createdAt).toLocaleDateString()}</TableCell>
                <TableCell align="right">
                  <IconButton size="small" onClick={() => handleEdit(film.filmId)}>
                    <EditIcon />
                  </IconButton>
                  <IconButton size="small" onClick={() => handleUpload(film.filmId)}>
                    <VideoLibraryIcon />
                  </IconButton>
                  <IconButton size="small" onClick={() => handleDelete(film.filmId)} color="error">
                    <DeleteIcon />
                  </IconButton>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <FilmEditDialog
        open={editDialogOpen}
        onClose={() => {
          setEditDialogOpen(false);
          setSelectedFilmId(null);
        }}
        filmId={selectedFilmId}
        mode="edit"
      />

      <FilmEditDialog
        open={createDialogOpen}
        onClose={() => {
          setCreateDialogOpen(false);
          setSelectedFilmId(null);
        }}
        filmId={null}
        mode="create"
      />

      <VideoUploadDialog
        open={uploadDialogOpen}
        onClose={() => {
          setUploadDialogOpen(false);
          setSelectedFilmId(null);
        }}
        filmId={selectedFilmId}
      />
    </Box>
  );
}

