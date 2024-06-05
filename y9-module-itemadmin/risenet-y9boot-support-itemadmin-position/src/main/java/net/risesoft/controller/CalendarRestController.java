package net.risesoft.controller;

import lombok.RequiredArgsConstructor;
import net.risesoft.consts.UtilConsts;
import net.risesoft.pojo.Y9Result;
import net.risesoft.service.CalendarConfigService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

/**
 * @author qinman
 * @author zhangchongjie
 * @date 2022/12/20
 */
@RestController
@RequiredArgsConstructor
@RequestMapping(value = "/vue/calendar")
public class CalendarRestController {

    
    private final CalendarConfigService calendarConfigService;

    /**
     * 删除日历配置
     *
     * @param startDate 日期
     * @return
     */
    @RequestMapping(value = "/delCalendar", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> delCalendar(@RequestParam(required = true) String startDate) {
        Map<String, Object> map = calendarConfigService.delCalendar(startDate);
        if ((boolean)map.get(UtilConsts.SUCCESS)) {
            return Y9Result.successMsg((String)map.get("message"));
        }
        return Y9Result.failure((String)map.get("message"));
    }

    /**
     * 获取日历配置
     *
     * @param month 月份
     * @return
     */
    @RequestMapping(value = "/getCalendar", method = RequestMethod.GET, produces = "application/json")
    public Y9Result<List<Map<String, Object>>> getCalendar(@RequestParam(required = true) String month) {
        List<Map<String, Object>> list = calendarConfigService.getCalendar(month);
        return Y9Result.success(list, "获取成功");
    }

    /**
     * 保存日历配置
     *
     * @param startDate 日期
     * @param type 类型
     * @return
     */
    @RequestMapping(value = "/saveCalendar", method = RequestMethod.POST, produces = "application/json")
    public Y9Result<String> saveCalendar(@RequestParam(required = true) String startDate,
        @RequestParam(required = true) Integer type) {
        Map<String, Object> map = calendarConfigService.saveCalendar(startDate, type);
        if ((boolean)map.get(UtilConsts.SUCCESS)) {
            return Y9Result.successMsg((String)map.get("message"));
        }
        return Y9Result.failure((String)map.get("message"));
    }
}
