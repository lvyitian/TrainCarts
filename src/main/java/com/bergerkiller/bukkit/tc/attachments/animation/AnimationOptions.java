package com.bergerkiller.bukkit.tc.attachments.animation;

import com.bergerkiller.bukkit.common.config.ConfigurationNode;

/**
 * Options for playing an animation to one or more attachments
 */
public class AnimationOptions implements Cloneable {
    private String _name;
    private double _speed;
    private double _delay;
    private boolean _looped;
    private boolean _hasLoopOption;

    protected AnimationOptions(AnimationOptions source) {
        this._name = source._name;
        this._speed = source._speed;
        this._delay = source._delay;
        this._looped = source._looped;
        this._hasLoopOption = source._hasLoopOption;
    }

    public AnimationOptions() {
        this("");
    }

    public AnimationOptions(String name) {
        this._name = name;
        this._speed = 1.0;
        this._delay = 0.0;
        this._looped = false;
        this._hasLoopOption = false;
    }

    /**
     * Sets the name of the animation to play
     * 
     * @param name of the animation to play
     */
    public void setName(String name) {
        this._name = name;
    }

    /**
     * Gets the name of the animation to play
     * 
     * @return animation name
     */
    public String getName() {
        return this._name;
    }

    /**
     * Gets the animation speed.
     * This is a multiplier to the durations specified in the nodes.
     * 
     * @return speed
     */
    public double getSpeed() {
        return this._speed;
    }

    /**
     * Sets a new animation speed.
     * This is a multiplier to the durations specified in the nodes.
     * 
     * @param speed
     */
    public void setSpeed(double speed) {
        this._speed = speed;
    }

    /**
     * Gets whether the animation is played in reverse.
     * This happens when the speed is negative.
     * 
     * @return True if reversed
     */
    public boolean isReversed() {
        return this._speed < 0.0;
    }

    /**
     * Gets the time delay until which this animation plays.
     * This delay is after the speed factor is applied.
     * 
     * @return animation start delay in seconds
     */
    public double getDelay() {
        return this._delay;
    }

    /**
     * Sets the time delay until which this animation plays.
     * This delay is after the speed factor is applied.
     * 
     * @param delay in seconds
     */
    public void setDelay(double delay) {
        this._delay = delay;
    }

    /**
     * Sets whether this animation is looped
     * 
     * @param looped
     */
    public void setLooped(boolean looped) {
        this._looped = looped;
        this._hasLoopOption = true;
    }

    /**
     * Resets the looped option, making it as if it was never set.
     * This assumes the looped setting of the animation itself, if set.
     */
    public void resetLooped() {
        this._hasLoopOption = false;
    }

    /**
     * Whether the looped option was at all specified using {@link #setLooped(boolean)}
     * 
     * @return True if looped setting was set
     */
    public boolean hasLoopOption() {
        return this._hasLoopOption;
    }

    /**
     * Gets whether this animation is looped
     * 
     * @return looped
     */
    public boolean isLooped() {
        return this._looped;
    }

    /**
     * Applies additional animation options to this one.
     * The speed, delay and looping options are updated.
     * 
     * @param options to apply
     */
    public void apply(AnimationOptions options) {
        this.setDelay(this.getDelay() + this.getSpeed() * options.getDelay());
        this.setSpeed(this.getSpeed() * options.getSpeed());
        if (options.hasLoopOption()) {
            this.setLooped(options.isLooped());
        }
    }

    /**
     * Loads the contents of these options from configuration.
     * The animation name is not loaded.
     * 
     * @param config to load from
     */
    public void loadFromConfig(ConfigurationNode config) {
        this._speed = config.contains("speed") ? config.get("speed", 1.0) : 1.0;
        this._delay = config.contains("delay") ? config.get("delay", 0.0) : 0.0;

        // Looped
        this._hasLoopOption = config.contains("looped");
        if (this._hasLoopOption) {
            this._looped = config.get("looped", false);
        } else {
            this._looped = false;
        }
    }

    /**
     * Saves the contents of these options to configuration.
     * The animation name is not saved.
     * 
     * @param config to save to
     */
    public void saveToConfig(ConfigurationNode config) {
        if (this._speed != 1.0) {
            config.set("speed", this._speed);
        }
        if (this._delay != 0.0) {
            config.set("delay", this._delay);
        }

        // Looped
        if (this._hasLoopOption) {
            config.set("looped", this._looped);
        } else {
            config.remove("looped");
        }
    }

    @Override
    public AnimationOptions clone() {
        return new AnimationOptions(this);
    }
}