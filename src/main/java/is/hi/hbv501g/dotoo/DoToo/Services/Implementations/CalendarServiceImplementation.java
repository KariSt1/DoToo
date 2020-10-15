package is.hi.hbv501g.dotoo.DoToo.Services.Implementations;

import is.hi.hbv501g.dotoo.DoToo.Entities.Event;
import is.hi.hbv501g.dotoo.DoToo.Repositories.EventRepository;
import is.hi.hbv501g.dotoo.DoToo.Services.CalendarService;

import java.util.List;
import java.util.Optional;

public class CalendarServiceImplementation implements CalendarService {
    EventRepository eventRepository;

    public Event save(Event event) {
        return eventRepository.save(event);
    }

    public void delete(Event event) {
        eventRepository.delete(event);
    }
    public List<Event> findAll() {
        return eventRepository.findAll();
    }
    public Optional<Event> findById(long id) {
        return eventRepository.findById(id);
    }
}
