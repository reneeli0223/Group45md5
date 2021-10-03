package com.sydney.vacbook.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.sydney.vacbook.entity.*;
import com.sydney.vacbook.mapper.AdminMapper;
import com.sydney.vacbook.mapper.UserMapper;
import com.sydney.vacbook.service.*;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import java.io.Serializable;
import java.util.*;

/**
 * @author shuonan wang
 * @since 2021-09-15
 */

//@RestController   如果加了这行注释 return 只会返回return里的实际内容、而不会跳转网页
@Controller
@RequestMapping("/vacbook/admin")
public class AdminController {


    @Autowired
    private IUserService iUserService;

    @Autowired
    private IAdminService iAdminService;

    @Autowired
    private IVaccineService iVaccineService;

    @Autowired
    private IBookingService iBookingService;

    @Autowired
    private ILocationService iLocationService;

    @Autowired
    VaccineController vaccineController;
    //一个adminList来判断登录合法性 并且存储相关信息
    List<Admin> listAdmin= new ArrayList<>();

    @GetMapping("{admin_id}/dashboard")
    public ModelAndView fetchDashboard(@PathVariable("admin_id") int admin_id) {
        System.out.print(admin_id);
        Admin admin = iAdminService.getById(admin_id);

        QueryWrapper<Vaccine> findVaccineByAdminId = new QueryWrapper<>();
        findVaccineByAdminId.lambda().eq(Vaccine::getAdminId, admin_id);
        List<Vaccine> vaccineList = iVaccineService.list(findVaccineByAdminId);
        List<String> vaccineNames = new ArrayList<>();
        List<Integer> vaccineIds = new ArrayList<>();
        for (Vaccine vaccine : vaccineList) {
            vaccineNames.add(vaccine.getVaccineName());
            vaccineIds.add(vaccine.getVaccineId());
        }

        int bookingNum = 0;
        for (Integer vaccineId : vaccineIds) {
            QueryWrapper<Booking> findBookingByVaccineId = new QueryWrapper<>();
            findBookingByVaccineId.lambda().eq(Booking::getVaccineId, vaccineId);
            bookingNum += iBookingService.count(findBookingByVaccineId);
        }

        Location location = iLocationService.getById(listAdmin.get(0).getLocationId());

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("account", listAdmin.get(0).getAdminAccount());
        result.put("name", listAdmin.get(0).getAdminName());
        result.put("location", location.getLocation());
        result.put("vaccines", vaccineNames);
        result.put("booking_num", bookingNum);
        System.out.print(result);
        ModelAndView modelAndView = new ModelAndView( "adminPages/dashboard","result", result);
        return modelAndView;
    }

    /**
     * @param admin_id
     //* @param body     body can used to get reject booking request
     * @return
     */

    @GetMapping("/{admin_id}/bookings")
    public ModelAndView fetchBookings(@PathVariable("admin_id") int admin_id) {

        Admin admin = iAdminService.getById(admin_id);
        if (admin != null) {
            QueryWrapper<Vaccine> findVaccineByAdminId = new QueryWrapper<>();
            List<Vaccine> vaccineList = iVaccineService.list(findVaccineByAdminId);
            List<String> vaccineNames = new ArrayList<>();
            List<Integer> vaccineIds = new ArrayList<>();
            for (Vaccine vaccine : vaccineList) {
                vaccineNames.add(vaccine.getVaccineName());
                vaccineIds.add(vaccine.getVaccineId());
            }
            QueryWrapper<Booking> queryWrapper = new QueryWrapper<>();
            queryWrapper.in("vaccine_id" , vaccineIds);
            List<Booking> bookingList = iBookingService.list(queryWrapper);
            ModelAndView modelAndView = new ModelAndView("adminPages/adminBooking" , "bookingList" , bookingList);
            return modelAndView;
            //return bookingList;
        }
        return null;
    }

    @PostMapping("/{admin_id}/bookings")
    public List<Booking> fetchBookings(@PathVariable("admin_id") int admin_id, @RequestBody Map<String, Object> body) {
        //TODO JAMES
        return null;
    }


    @GetMapping("/{admin_id}/booking/user/{user_id}")
    public ModelAndView fetchBookingUser(@PathVariable("user_id") int user_id) {
        User user = iUserService.getById(user_id);
        ModelAndView modelAndView = new ModelAndView( "adminPages/booking_user","result", user);
        return modelAndView;
    }

    /**
     * @param admin_id
     //* @param body     body can used to get add, delete, update requests based on the design of figma
     * @return
     */
    @GetMapping("/{admin_id}/vaccines")
    public ModelAndView fetchVaccines(@PathVariable("admin_id") int admin_id/*, @RequestBody Map<String, Object> body*/) {
        //TODO ZHENGCHENG

        List<Vaccine> resultSet = vaccineController.getVaccineListByAdminId(listAdmin.get(0).getAdminId());
        ModelAndView modelAndView = new ModelAndView( "adminPages/adminVaccines","adminVaccineList", resultSet);
        return modelAndView;
    }

    @GetMapping("/{admin_id}/setting")
    public ModelAndView fetchSetting(@PathVariable("admin_id") int admin_id){
        Admin admin = iAdminService.getById(admin_id);
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("admin_id", listAdmin.get(0).getAdminId());
        result.put("account", listAdmin.get(0).getAdminAccount());
        result.put("name", listAdmin.get(0).getAdminName());
        Location location = iLocationService.getById(listAdmin.get(0).getLocationId());
        result.put("location", location.getLocation());
        List<Location> locationList = iLocationService.list();
        result.put("location_options", locationList);

        ModelAndView modelAndView = new ModelAndView( "adminPages/setting","result", result);
        return modelAndView;
    }

    @PutMapping("/{admin_id}/setting")
    public boolean udateSetting( @PathVariable("admin_id") int admin_id, @RequestBody Map<String, Object> body) {
        //ModelAndView modelAndView = new ModelAndView( "adminPages/setting", new );
        //if body has content, update admin information
        System.out.println(body);
        Admin admin = iAdminService.getById(admin_id);
        if (!body.isEmpty()) {
            System.out.println(body);
            admin.updateByMap(body);
            iAdminService.saveOrUpdate(admin);
            return  true;
        }
        return false;
    }
//这个地方改成index了
    @RequestMapping("/index")
    public String index(){
        return "adminPages/adminLogin";
    }
//跳转到login  --- 但是现在登录成功的return我有点迷惑 报500错误
    @RequestMapping("/login")
    public String login(@RequestParam String account,String password, Map<String, Object> map) {
        System.out.println("1111111111111111111111111111");
        //TODO WORDE
        QueryWrapper<Admin> sectionQueryWrapper = new QueryWrapper<>();
        sectionQueryWrapper.eq("admin_account", account);
        sectionQueryWrapper.eq("admin_password", password);
        listAdmin = iAdminService.list(sectionQueryWrapper);

        String str = listAdmin.toString();

        if (!str.equals("[]")) {

            map.put("adminList", listAdmin.get(0));
//下面写登录后想要获得的更多东西例如获取疫苗
           // map.put("vaccineList", vaccineController.getVaccineListByAdminId(admin.getAdminId()));

            return "adminPages/base";//重定向
        } else {

            return "adminPages/adminLogin";//重定向
        }

    }

//    @GetMapping("/login")
//    public ModelAndView getAdminLoginPage(){
//        ModelAndView modelAndView = new ModelAndView( "adminPages/adminLogin");
//        return modelAndView;
//    }

//    @GetMapping("/register")
//    public ModelAndView getAdminRegisterPage(){
//        ModelAndView modelAndView = new ModelAndView( "adminPages/adminRegister");
//        return modelAndView;
//    }

    @RequestMapping("/registerPage")
    public String registerPage(){
        return "adminPages/adminRegister";
    }

    @RequestMapping("/register")
    public String register(Admin admin, Map<Object, Object> body) {
        System.out.println("===============");
        boolean newAdmin = iAdminService.save(admin);
        if (newAdmin == false) {
            System.err.println("This account has been registered");
            return "redirect:index";//重定向
        } else {
            System.out.println("Thanks for join our system");

            QueryWrapper<Admin> sectionQueryWrapper = new QueryWrapper<>();
            sectionQueryWrapper.eq("admin_account", admin.getAdminAccount());
            sectionQueryWrapper.eq("admin_password", admin.getAdminPassword());
            listAdmin = iAdminService.list(sectionQueryWrapper);

            body.put("adminList", listAdmin.get(0));


            return "adminPages/base";
        }


    }

    @RequestMapping("/logout")
    public String  logout(Map<Object, Object> map) {
        map.put("adminList","");
        return "redirect:index";// 重定向


    }

//    取信息
//    @RequestMapping("/adminList")
//    public String  listAdmin(Model model) {
//       model.addAttribute("listAdmin",listAdmin.get(0));
//       return "admin/list";
//
//    }


// exercises
//    @GetMapping("{id}/getUserByAge")
//    public List<User> getUserByAge(@PathVariable("id") int id, @RequestParam("age") int age) {
//        System.out.println("hellp");
//
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        //lambda写法:
//        queryWrapper.lambda().eq(User::getAge, age);
//
//
//        List<User> userList = iUserService.list(queryWrapper);
//        return userList;
//    }
//
//    @GetMapping("/getUserListByAgeLimit")
//    public List<User> getUserListByAgeLimit() {
//
//        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
//        queryWrapper.lambda().gt(User::getAge, 1);
//        queryWrapper.lambda().lt(User::getAge, 25);
//
//        List<User> userList = iUserService.list(queryWrapper);
//        return userList;
//    }

//    @GetMapping("{id}/update")
//    public void updateAdmin(@PathVariable("id") int id) {
//        Admin admin = iAdminService.getById(id);
//        admin.setAdminName("super_kevin");
//        iAdminService.saveOrUpdate(admin);
//    }


}
