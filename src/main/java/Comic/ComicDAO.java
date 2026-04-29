package Comic;

import java.util.List;
import java.util.Optional;

public interface ComicDAO {
    Optional<Comic> getComicById(int comicID);
    List<Comic> getAllComics();
    List<Comic> getComicsByPublisher(String publisher);
    Optional<Comic> getComicsByName(String name);
    void saveComic(Comic comic);
    void updateComic(Comic comic);
    void deleteComicByID(int comicID);

}
