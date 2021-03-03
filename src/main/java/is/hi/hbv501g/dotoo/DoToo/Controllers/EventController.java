package is.hi.hbv501g.dotoo.DoToo.Controllers;

import is.hi.hbv501g.dotoo.DoToo.Entities.Event;
import is.hi.hbv501g.dotoo.DoToo.Entities.User;
import is.hi.hbv501g.dotoo.DoToo.Services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

@Controller
public class EventController {

    private EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @RequestMapping("/events")
    public String EventPage(Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        LocalDate date = LocalDate.now();
        LocalDate viewedDate = (LocalDate) session.getAttribute("date");
        if (viewedDate == null) {
            viewedDate = date;
        }

        String category = (String) session.getAttribute("category");
        if (category == null) {
            session.setAttribute("category", "All");
            category = "All";
        }

        WeekFields weekFields = WeekFields.of(Locale.UK);
        model.addAttribute("date", date);
        String view = (String) session.getAttribute("view");

        if (view != null) {
            model.addAttribute("view", view);
            session.setAttribute("view", view);
        } else {
            view = "week";
            model.addAttribute("view", "week");
            session.setAttribute("view", "week");
        }

        Integer offset = (Integer) session.getAttribute("offset");
        if (offset == null) {
            offset = 0;
        }


        if (view.equals("day")) {
            if (offset > 0) {
                viewedDate = viewedDate.plusDays(1);
            } else if (offset < 0) {
                viewedDate = viewedDate.minusDays(1);
            }
            model.addAttribute("events", eventService.findByDay(viewedDate.getYear(), viewedDate.getMonthValue(), viewedDate.getDayOfMonth(), category, sessionUser));
        } else if (view.equals("week")) {
            if (offset > 0) {
                viewedDate = viewedDate.plusWeeks(1);
            } else if (offset < 0) {
                viewedDate = viewedDate.minusWeeks(1);
            }
            model.addAttribute("events", eventService.findByWeek(viewedDate.getYear(), viewedDate.get(weekFields.weekOfWeekBasedYear()), category, sessionUser));
            model.addAttribute("weekStart", viewedDate.with(weekFields.dayOfWeek(), 1L));
            model.addAttribute("weekEnd", viewedDate.with(weekFields.dayOfWeek(), 7L));
        } else if (view.equals("month")) {
            if (offset > 0) {
                viewedDate = viewedDate.plusMonths(1);
            } else if (offset < 0) {
                viewedDate = viewedDate.minusMonths(1);
            }
            model.addAttribute("events", eventService.findByMonth(viewedDate.getYear(), viewedDate.getMonthValue(), category, sessionUser));
        }

        session.setAttribute("offset", 0);
        session.setAttribute("date", viewedDate);
        model.addAttribute("date", viewedDate);
        model.addAttribute("loggedinuser", sessionUser);
        model.addAttribute("category", category);
        return "EventPage";
    }

    @RequestMapping("/changeview")
    public String changeEventView(@RequestParam(value = "viewDate", required = false) String viewDate, @RequestParam(value = "view", required = false) String view,
                                  @RequestParam(value = "nav", required = false) String nav, @RequestParam(value = "category", required = false) String category, Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        int offset = 0;

        if (sessionUser == null) {
            return "redirect:/login";
        }
        if (viewDate.length() > 0) {
            LocalDate date = LocalDate.parse(viewDate);
            session.setAttribute("date", date);
        }

        if (category == null || category.equals("")) {
            category = session.getAttribute("category").toString();
        }

        if (view == null || view.equals("")) {
            view = session.getAttribute("view").toString();
        }

        if (nav == null || nav.equals("")) offset = 0;
        else if (nav.equals("next")) offset = 1;
        else if (nav.equals("prev")) offset = -1;

        session.setAttribute("view", view);
        session.setAttribute("offset", offset);
        session.setAttribute("category", category);
        return "redirect:/events";
    }

    @RequestMapping("/makenewevent")
    public String makeEvent(@RequestParam(value = "startDate") String startDate, @RequestParam(value = "endDate") String endDate,
                            @RequestParam(value = "title") String title, @RequestParam(value = "category") String category,
                            @RequestParam(value = "color") String color, HttpSession session) throws ParseException {
        Calendar sd = Calendar.getInstance();
        Calendar ed = Calendar.getInstance();
        startDate = startDate.replace(startDate.charAt(10), ' ');
        endDate = endDate.replace(endDate.charAt(10), ' '); //Get rid of the T from date string
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.ENGLISH);
        sd.setTime(sdf.parse(startDate));
        ed.setTime(sdf.parse(endDate));

        Event event = new Event(sd, ed, title, category, color, (User) session.getAttribute("loggedInUser"));
        session.setAttribute("offset", 0);
        eventService.save(event);
        return "redirect:/events";
    }

    @RequestMapping(value = "/deleteEvent", method = RequestMethod.POST)
    public String deleteEvent(@RequestParam(value = "id") long id) {
        Event event = eventService.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid event id"));
        eventService.delete(event);
        return "redirect:/events";
    }
}
