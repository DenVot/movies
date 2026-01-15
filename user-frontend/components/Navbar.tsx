'use client';

import { useRouter } from 'next/navigation';
import { AppBar, Toolbar, Typography, Button, Box } from '@mui/material';
import { useAuthStore } from '@/store/authStore';

export default function Navbar() {
    const router = useRouter();
    const { isAuthenticated, user, logout } = useAuthStore();

    const handleLogout = async () => {
        await logout();
        router.push('/login');
    };

    return (
        <AppBar position="static">
            <Toolbar>
                <Typography variant="h6" component="div" sx={{ flexGrow: 1 }}>
                    Онлайн Кинотеатр
                </Typography>
                {isAuthenticated && (
                    <Box sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
                        {user && (
                            <Typography variant="body2">
                                {user.username}
                            </Typography>
                        )}
                        <Button color="inherit" onClick={handleLogout}>
                            Выйти
                        </Button>
                    </Box>
                )}
            </Toolbar>
        </AppBar>
    );
}

