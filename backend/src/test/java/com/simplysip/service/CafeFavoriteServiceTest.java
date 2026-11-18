package com.simplysip.service;

import com.simplysip.exception.ConflictException;
import com.simplysip.exception.NotFoundException;
import com.simplysip.model.CafeFavorite;
import com.simplysip.model.CafeFavoriteId;
import com.simplysip.model.Location;
import com.simplysip.model.User;
import com.simplysip.repository.CafeFavoriteRepository;
import com.simplysip.repository.LocationRepository;
import com.simplysip.repository.UserRepository;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CafeFavoriteServiceTest {
    private final CafeFavoriteRepository repo = mock(CafeFavoriteRepository.class);
    private final UserRepository users = mock(UserRepository.class);
    private final LocationRepository locations = mock(LocationRepository.class);
    private final CafeFavoriteService service = new CafeFavoriteService(repo, users, locations);

    private void existingCafeAndUser() {
        User user = new User();
        user.setId(1L);
        Location location = new Location();
        location.setId(4L);
        when(users.findById(1L)).thenReturn(Optional.of(user));
        when(locations.findById(4L)).thenReturn(Optional.of(location));
    }

    @Test
    void savesCafeForSpecifiedOwner() {
        existingCafeAndUser();
        when(repo.save(any(CafeFavorite.class))).thenAnswer(invocation -> invocation.getArgument(0));
        assertEquals(4L, service.save(1L, 4L).locationId());
        verify(repo).save(argThat(f -> f.getId().equals(new CafeFavoriteId(1L, 4L))));
    }

    @Test
    void duplicateCafeReturnsConflict() {
        existingCafeAndUser();
        when(repo.existsById(new CafeFavoriteId(1L, 4L))).thenReturn(true);
        assertThrows(ConflictException.class, () -> service.save(1L, 4L));
        verify(repo, never()).save(any());
    }

    @Test
    void listsOnlyFavoritesForRequestedUser() {
        when(repo.findByUser_Id(1L)).thenReturn(List.of());
        assertEquals(List.of(), service.list(1L));
        verify(repo).findByUser_Id(1L);
    }

    @Test
    void removingMissingFavoriteReturnsNotFound() {
        assertThrows(NotFoundException.class, () -> service.remove(1L, 4L));
        verify(repo, never()).deleteById(any());
    }

    @Test
    void removesOnlyOwnerCafePair() {
        CafeFavoriteId pair = new CafeFavoriteId(1L, 4L);
        when(repo.existsById(pair)).thenReturn(true);
        service.remove(1L, 4L);
        verify(repo).deleteById(pair);
    }
}
