package fr.ensibs.robots.impl;

import java.awt.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * MISSION F: Manages floating damage numbers that appear when robots take damage.
 * 
 * @author Robot Wars Team
 */
public class DamageNumberManager {
    
    private static class DamageNumber {
        double x, y;
        int damage;
        int lifetime;
        float alpha;
        
        DamageNumber(double x, double y, int damage) {
            this.x = x;
            this.y = y + (Math.random() - 0.5) * 20; // Slight random offset
            this.damage = damage;
            this.lifetime = 60; // 1 second at 60 FPS
            this.alpha = 1.0f;
        }
        
        void update() {
            y -= 1; // Float upward
            lifetime--;
            alpha = Math.max(0, lifetime / 60.0f);
        }
        
        boolean isDead() {
            return lifetime <= 0;
        }
    }
    
    private final List<DamageNumber> numbers = new ArrayList<>();
    
    public synchronized void addDamage(double x, double y, int damage) {
        numbers.add(new DamageNumber(x, y, damage));
    }
    
    public synchronized void update() {
        Iterator<DamageNumber> iter = numbers.iterator();
        while (iter.hasNext()) {
            DamageNumber num = iter.next();
            num.update();
            if (num.isDead()) {
                iter.remove();
            }
        }
    }
    
    public void draw(Graphics2D g) {
        List<DamageNumber> snapshot;
        synchronized (this) {
            snapshot = new ArrayList<>(numbers);
        }

        g.setFont(new Font("Monospaced", Font.BOLD, 16));
        
        for (DamageNumber num : snapshot) {
            g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, num.alpha));
            g.setColor(Color.RED);
            g.drawString("-" + num.damage, (int)num.x, (int)num.y);
        }
        
        g.setComposite(AlphaComposite.SrcOver);
    }
}
