package com.nutritrack;

import com.nutritrack.dao.ConsumptionDao;
import com.nutritrack.dao.FoodDao;
import com.nutritrack.dao.TargetDao;
import com.nutritrack.dao.UserDao;
import com.nutritrack.database.DatabaseConnection;
import com.nutritrack.service.FoodService;
import com.nutritrack.service.NutritionService;
import com.nutritrack.service.ReportService;
import com.nutritrack.service.UserService;
import com.nutritrack.ui.MainMenu;

public class Main {
    public static void main(String[] args) {
        var connectionFactory = new DatabaseConnection();
        var userService = new UserService(new UserDao(connectionFactory));
        var foodService = new FoodService(new FoodDao(connectionFactory));
        var nutritionService = new NutritionService(
                new ConsumptionDao(connectionFactory), new TargetDao(connectionFactory));
        new MainMenu(userService, foodService, nutritionService, new ReportService()).run();
    }
}
