package Comic;

import java.util.List;
import java.util.Optional;

/**
 * Database contract for comic records.
 * Controllers depend on this interface so the SQL details stay inside the DAO implementation.
 */
public interface ComicDAO {
    Optional<Comic> getComicById(int comicID);
    List<Comic> getAllComics();
    List<Comic> getComicsByPublisher(String publisher);
    Optional<Comic> getComicsByName(String name);
    void saveComic(Comic comic);
    void updateComic(Comic comic);
    void deleteComicByID(int comicID);

}
