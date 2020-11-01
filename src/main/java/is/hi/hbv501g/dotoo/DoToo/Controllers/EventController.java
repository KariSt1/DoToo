package is.hi.hbv501g.dotoo.DoToo.Controllers;

import is.hi.hbv501g.dotoo.DoToo.Entities.Event;
import is.hi.hbv501g.dotoo.DoToo.Entities.TodoList;
import is.hi.hbv501g.dotoo.DoToo.Entities.User;
import is.hi.hbv501g.dotoo.DoToo.Services.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.WeekFields;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

@Controller
public class EventController {

    private EventService eventService;
    
    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @RequestMapping("/calendar")
    public String CalendarPage(Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        Calendar now = Calendar.getInstance();
        now.setTimeZone(TimeZone.getTimeZone("GMT"));
        model.addAttribute("day", now.get(Calendar.DAY_OF_MONTH));
        model.addAttribute("week", now.get(Calendar.WEEK_OF_YEAR));
        model.addAttribute("month", now.get(Calendar.MONTH));
        model.addAttribute("year", now.get(Calendar.YEAR));
        model.addAttribute("loggedinuser", sessionUser);
        model.addAttribute("view", "week");
        model.addAttribute("events", eventService.findByWeek(now.get(Calendar.YEAR), now.get(Calendar.WEEK_OF_YEAR), sessionUser));
        session.setAttribute("view", "week");
        session.setAttribute("offset", 0);

        return "CalendarPage";
    }

    /**
     * Handles changes of view
     * Shows list of events of chosen day, month or year
     * @param view day, month or year
     * @param nav prev or next - goes back or forward in time
     * @param model
     * @param session
     * @return CalendarPage
     */
    @RequestMapping("/changeview")
    public String changeCalendarView(@RequestParam(value="view", required = false) String view, @RequestParam(value="nav", required = false) String nav, Model model, HttpSession session) {
        User sessionUser = (User) session.getAttribute("loggedInUser");
        if (sessionUser == null) {
            return "redirect:/login";
        }
        if(view == null) {
            view = session.getAttribute("view").toString();
        }
        int offset = Integer.valueOf(session.getAttribute("offset").toString());
        if(nav == null) offset += 0;
        else if(nav.equals("next")) offset += 1;
        else if(nav.equals("prev")) offset -= 1;
        else offset = 0;

        //Calendar now = Calendar.getInstance();
        LocalDate now = LocalDate.now();
        WeekFields weekFields = WeekFields.of(Locale.getDefault());
        //now.setTimeZone(TimeZone.getTimeZone("GMT"));
        int year = now.getYear();
        int month = now.getMonthValue();
        int week = now.get(weekFields.weekOfWeekBasedYear());
        int day = now.getDayOfMonth();


        model.addAttribute("day", day);
        model.addAttribute("week", week);
        model.addAttribute("month", month);
        model.addAttribute("year", year);

        if (view.equals("day")) {
            LocalDate newDate;
            if(offset < 0) {
                newDate = now.minus(Period.ofDays(Math.abs(offset)));
            } else {
                newDate = now.plus(Period.ofDays(offset));
            }
            model.addAttribute("events", eventService.findByDay(newDate.getYear(), newDate.getMonthValue(), newDate.getDayOfMonth(), sessionUser));
            model.addAttribute("view", "day");
            model.addAttribute("day", newDate.getDayOfMonth());
            session.setAttribute("view", "day");
        }

        else if(view.equals("week")) {
            LocalDate newDate;
            if(offset < 0) {
                newDate = now.minus(Period.ofWeeks(Math.abs(offset)));
            } else {
                newDate = now.plus(Period.ofWeeks(offset));
            }
            model.addAttribute("events", eventService.findByWeek(newDate.getYear(), newDate.get(weekFields.weekOfWeekBasedYear()), sessionUser));
            model.addAttribute("view", "week");
            model.addAttribute("week", newDate.get(weekFields.weekOfWeekBasedYear()));
            session.setAttribute("view", "week");
        }

        else if(view.equals("month")) {
            LocalDate newDate;
            if(offset < 0) {
                newDate = now.minus(Period.ofMonths(Math.abs(offset)));
            } else {
                newDate = now.plus(Period.ofMonths(offset));
            }
            model.addAttribute("events", eventService.findByMonth(newDate.getYear(), newDate.getMonthValue(), sessionUser));
            model.addAttribute("view", "month");
            model.addAttribute("month", newDate.getMonthValue());
            session.setAttribute("view", "month");
        }

        // Not implemented in interface for now
        else if(view.equals("year")) {
            LocalDate newDate;
            if(offset < 0) {
                newDate = now.minus(Period.ofMonths(offset));
            } else {
                newDate = now.plus(Period.ofMonths(offset));
            }
            model.addAttribute("events", eventService.findByYear(newDate.getYear(), sessionUser));
            model.addAttribute("view", "year");
            model.addAttribute("year", newDate.getYear());
            session.setAttribute("view", "year");
        }

        session.setAttribute("offset", offset);
        model.addAttribute("offset", offset);
        model.addAttribute("loggedinuser", sessionUser);
        return "CalendarPage";
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
        eventService.save(event);
        return "redirect:/calendar";
    }

    @RequestMapping(value="/calendar/delete/{id}", method = RequestMethod.GET)
    public String deleteEvent(@PathVariable("id") long id, Model model) {
        Event event = eventService.findById(id).orElseThrow(() -> new IllegalArgumentException("Invalid event id"));
        eventService.delete(event);
        model.addAttribute("events", eventService.findAll());
        return "CalendarPage";
    }
}
