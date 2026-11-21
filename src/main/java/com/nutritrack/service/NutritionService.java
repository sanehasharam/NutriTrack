package com.nutritrack.service;

import com.nutritrack.dao.ConsumptionDao;
import com.nutritrack.dao.TargetDao;
import com.nutritrack.model.Consumption;
import com.nutritrack.model.MealCategory;
import com.nutritrack.model.NutritionSummary;
import com.nutritrack.model.NutritionTarget;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class NutritionService {
    private final ConsumptionDao consumptions;
    private final TargetDao targets;

    public NutritionService(ConsumptionDao consumptions, TargetDao targets) {
        this.consumptions = consumptions;
        this.targets = targets;
    }

    public void log(long userId, long foodId, MealCategory meal, BigDecimal quantity) throws SQLException {
        if (foodId <= 0 || quantity == null || quantity.signum() <= 0) {
            throw new IllegalArgumentException("Food ID and quantity must be greater than zero.");
        }
        consumptions.create(userId, foodId, meal, quantity);
    }

    public NutritionSummary dailySummary(long userId, LocalDate date) throws SQLException {
        List<Consumption> entries = consumptions.findByUserAndDate(userId, date);
        return new NutritionSummary(entries,
                sum(entries, Consumption::calories), sum(entries, Consumption::protein),
                sum(entries, Consumption::carbohydrates), sum(entries, Consumption::fat));
    }

    public void saveTargets(long userId, NutritionTarget target) throws SQLException {
        if (target == null || target.calories() == null || target.protein() == null
                || target.carbohydrates() == null || target.fat() == null
                || target.calories().signum() < 0 || target.protein().signum() < 0
                || target.carbohydrates().signum() < 0 || target.fat().signum() < 0) {
            throw new IllegalArgumentException("All nutrition targets must be zero or greater.");
        }
        targets.save(userId, target);
    }

    public NutritionTarget targets(long userId) throws SQLException {
        return targets.findByUser(userId).orElse(null);
    }

    private BigDecimal sum(List<Consumption> entries,
                           java.util.function.Function<Consumption, BigDecimal> amount) {
        return entries.stream().map(amount).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
