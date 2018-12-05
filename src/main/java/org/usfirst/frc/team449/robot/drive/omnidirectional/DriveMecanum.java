package org.usfirst.frc.team449.robot.drive.omnidirectional;

import com.fasterxml.jackson.annotation.JsonProperty;
import edu.wpi.first.wpilibj.command.Subsystem;
import edu.wpi.first.wpilibj.drive.RobotDriveBase;
import edu.wpi.first.wpilibj.drive.Vector2d;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.usfirst.frc.team449.robot.components.MecanumComponent;
import org.usfirst.frc.team449.robot.generalInterfaces.loggable.Loggable;
import org.usfirst.frc.team449.robot.jacksonWrappers.FPSTalon;
import org.usfirst.frc.team449.robot.jacksonWrappers.MappedAHRS;
import org.usfirst.frc.team449.robot.subsystem.interfaces.AHRS.SubsystemAHRS;

/**
 * A mecanum drive, which uses mecanum wheels to drive omnidirectionally.
 */
public class DriveMecanum extends Subsystem implements SubsystemAHRS, DriveOmnidirectional, Loggable {

	/**
	 * The front right, front left, rear left, and rear right Talons.
	 */
	@NotNull
	private final FPSTalon frontRightMotor, frontLeftMotor, rearLeftMotor, rearRightMotor;

	/**
	 * The NavX gyro
	 */
	@NotNull
	private final MappedAHRS ahrs;

	/**
	 * A component to convert desired velocities to motor outputs.
	 */
	@Nullable
	private MecanumComponent component;

	/**
	 * Whether or not to use the NavX for driving straight
	 */
	private boolean overrideGyro;

	/**
	 * Cached values for various sensor readings.
	 */
	@Nullable
	private Double cachedFrontRightMotorVel, cachedFrontLeftMotorVel, cachedRearMotorVel, cachedRearRightMotorVel,
				   cachedFrontRightMotorPos, cachedFrontLeftMotorPos, cachedRearMotorPos, cachedRearRightMotorPos;

	/**
	 * Creates a subsystem. This will set the name to the name of the class.
	 */
	public DriveMecanum(@NotNull @JsonProperty(required = true) FPSTalon frontRightMotor,
	                    @NotNull @JsonProperty(required = true) FPSTalon frontLeftMotor,
	                    @NotNull @JsonProperty(required = true) FPSTalon rearLeftMotor,
	                    @NotNull @JsonProperty(required = true) FPSTalon rearRightMotor,
	                    @NotNull @JsonProperty(required = true) MappedAHRS ahrs) {
		this.frontRightMotor = frontRightMotor;
		this.frontLeftMotor = frontLeftMotor;
		this.rearLeftMotor = rearLeftMotor;
		this.rearRightMotor = rearRightMotor;
		this.ahrs = ahrs;
		component = new MecanumComponent();
		overrideGyro = false;
	}

	/**
	 * Initialize the default command for a subsystem By default subsystems have no default command,
	 * but if they do, the default command is set with this method. It is called on all Subsystems by
	 * CommandBase in the users program after all the Subsystems are created.
	 */
	@Override
	protected void initDefaultCommand() {
		//Do nothing, the default command gets set with setDefaultCommandManual
	}

	/**
	 * Set the forward, strafe, and rotational velocities.
	 *
	 * @param desiredLongitudinalVelocity The desired forward-backward velocity, on [-1, 1].
	 * @param desiredLateralVelocity      The desired left-right velocity, on [-1, 1].
	 * @param desiredRotationalVelocity   The desired counter-clockwise positive rotations/sec, on [-1, 1].
	 */
	@Override
	public void setDirection(double desiredLongitudinalVelocity, double desiredLateralVelocity, double desiredRotationalVelocity) {
		component.updateDesiredVelocities(desiredLongitudinalVelocity, desiredLateralVelocity, desiredRotationalVelocity, ahrs.getAngularDisplacement());
		double[] motorOutputs = component.calculateMotorOutputs();
		frontRightMotor.setVelocity(motorOutputs[0]);
		frontLeftMotor.setVelocity(motorOutputs[1]);
		rearLeftMotor.setVelocity(motorOutputs[2]);
		rearRightMotor.setVelocity(motorOutputs[3]);
	}

	/**
	 * Completely stop the robot by setting the voltage to each side to be 0.
	 */
	@Override
	public void fullStop() {
		frontRightMotor.setPercentVoltage(0);
		frontLeftMotor.setPercentVoltage(0);
		rearLeftMotor.setPercentVoltage(0);
		rearRightMotor.setPercentVoltage(0);
	}

	/**
	 * If this drive uses motors that can be disabled, enable them.
	 */
	@Override
	public void enableMotors() {
		frontRightMotor.enable();
		frontLeftMotor.enable();
		rearLeftMotor.enable();
		rearRightMotor.enable();
	}

	/**
	 * Reset the position of the drive if it has encoders.
	 */
	@Override
	public void resetPosition() {
		frontRightMotor.resetPosition();
		frontLeftMotor.resetPosition();
		rearLeftMotor.resetPosition();
		rearRightMotor.resetPosition();
	}

	/**
	 * Get the headers for the data this subsystem logs every loop.
	 *
	 * @return An N-length array of String labels for data, where N is the length of the Object[] returned by getData().
	 */
	@NotNull
	@Override
	public String[] getHeader() {
		return new String[0];
	}

	/**
	 * Get the data this subsystem logs every loop.
	 *
	 * @return An N-length array of Objects, where N is the number of labels given by getHeader.
	 */
	@NotNull
	@Override
	public Object[] getData() {
		return new Object[0];
	}

	/**
	 * Get the name of this object.
	 *
	 * @return A string that will identify this object in the log file.
	 */
	@Override
	public @NotNull String getLogName() {
		return "Drive";
	}

	/**
	 * Updates all cached values with current ones.
	 */
	@Override
	public void update() {
		cachedFrontRightMotorVel = getRearRightMotorVel();
		cachedFrontLeftMotorVel = getFrontLeftMotorVel();
		cachedRearMotorVel = getRearRightMotorVel();
		cachedRearRightMotorVel = getRearRightMotorVel();
		cachedFrontRightMotorPos = getFrontRightMotorPos();
		cachedFrontLeftMotorPos = getFrontLeftMotorPos();
		cachedRearMotorPos = getRearRightMotorPos();
		cachedRearRightMotorPos = getRearRightMotorPos();
	}

	/**
	 * Get the robot's heading.
	 *
	 * @return robot heading, in degrees, on [-180, 180].
	 */
	@Override
	public double getHeading() {
		return ahrs.getHeading();
	}

	/**
	 * Set the robot's heading.
	 *
	 * @param heading The heading to set to, in degrees on [-180, 180].
	 */
	@Override
	public void setHeading(double heading) {
		ahrs.setHeading(heading);
	}

	/**
	 * Get the robot's cached heading.
	 *
	 * @return robot heading, in degrees, on [-180, 180].
	 */
	@Override
	public double getHeadingCached() {
		return ahrs.getCachedHeading();
	}

	/**
	 * Get the robot's angular velocity.
	 *
	 * @return Angular velocity in degrees/sec
	 */
	@Override
	public double getAngularVel() {
		return ahrs.getAngularVelocity();
	}

	/**
	 * Get the robot's cached angular velocity.
	 *
	 * @return Angular velocity in degrees/sec
	 */
	@Override
	public double getAngularVelCached() {
		return ahrs.getCachedAngularVelocity();
	}

	/**
	 * Get the robot's angular displacement since being turned on.
	 *
	 * @return Angular displacement in degrees.
	 */
	@Override
	public double getAngularDisplacement() {
		return ahrs.getAngularDisplacement();
	}

	/**
	 * Get the robot's cached angular displacement since being turned on.
	 *
	 * @return Angular displacement in degrees.
	 */
	@Override
	public double getAngularDisplacementCached() {
		return ahrs.getCachedAngularDisplacement();
	}

	/**
	 * Get the pitch value.
	 *
	 * @return The pitch, in degrees from [-180, 180]
	 */
	@Override
	public double getPitch() {
		return ahrs.getPitch();
	}

	/**
	 * Get the cached pitch value.
	 *
	 * @return The pitch, in degrees from [-180, 180]
	 */
	@Override
	public double getCachedPitch() {
		return ahrs.getCachedPitch();
	}

	/**
	 * @return true if the gyroscope is currently overriden, false otherwise.
	 */
	@Override
	public boolean getOverrideGyro() {
		return overrideGyro;
	}

	/**
	 * @param override true to override the gyro, false to un-override it.
	 */
	@Override
	public void setOverrideGyro(boolean override) {
		overrideGyro = override;
	}

	/**
	 * Get the velocity of the front right motor.
	 *
	 * @return The signed velocity in feet per second, or null if the drive doesn't have encoders.
	 */
	@Nullable
	private Double getFrontRightMotorVel() {
		return frontRightMotor.getVelocity();
	}

	/**
	 * Get the velocity of the front left motor.
	 *
	 * @return The signed velocity in feet per second, or null if the drive doesn't have encoders.
	 */
	@Nullable
	private Double getFrontLeftMotorVel() {
		return frontLeftMotor.getVelocity();
	}

	/**
	 * Get the velocity of the rear left motor.
	 *
	 * @return The signed velocity in feet per second, or null if the drive doesn't have encoders.
	 */
	@Nullable
	private Double getRearLeftMotorVel() {
		return rearLeftMotor.getVelocity();
	}

	/**
	 * Get the velocity of the rear right motor.
	 *
	 * @return The signed velocity in feet per second, or null if the drive doesn't have encoders.
	 */
	@Nullable
	private Double getRearRightMotorVel() {
		return rearRightMotor.getVelocity();
	}

	/**
	 * Get the position of the front right motor.
	 *
	 * @return The signed position in feet, or null if the drive doesn't have encoders.
	 */
	@Nullable
	private Double getFrontRightMotorPos() {
		return frontRightMotor.getPositionFeet();
	}

	/**
	 * Get the position of the front left motor.
	 *
	 * @return The signed position in feet, or null if the drive doesn't have encoders.
	 */
	@Nullable
	private Double getFrontLeftMotorPos() {
		return frontLeftMotor.getPositionFeet();
	}

	/**
	 * Get the position of the rear left motor.
	 *
	 * @return The signed position in feet, or null if the drive doesn't have encoders.
	 */
	@Nullable
	private Double getRearLeftMotorPos() {
		return rearLeftMotor.getPositionFeet();
	}

	/**
	 * Get the position of the rear right motor.
	 *
	 * @return The signed position in feet, or null if the drive doesn't have encoders.
	 */
	@Nullable
	private Double getRearRightMotorPos() {
		return rearRightMotor.getPositionFeet();
	}

	/**
	 * Get the cached velocity of the front right motor.
	 *
	 * @return The signed velocity in feet per second, or null if the drive doesn't have encoders.
	 */
	@Nullable
	private Double getFrontRightMotorVelCached() {
		return frontRightMotor.getVelocity();
	}

	/**
	 * Get the cached velocity of the front left motor.
	 *
	 * @return The signed velocity in feet per second, or null if the drive doesn't have encoders.
	 */
	@Nullable
	private Double getFrontLeftMotorVelCached() {
		return frontLeftMotor.getVelocity();
	}

	/**
	 * Get the cached velocity of the rear left motor.
	 *
	 * @return The signed velocity in feet per second, or null if the drive doesn't have encoders.
	 */
	@Nullable
	private Double getRearLeftMotorVelCached() {
		return rearLeftMotor.getVelocity();
	}

	/**
	 * Get the cached velocity of the rear right motor.
	 *
	 * @return The signed velocity in feet per second, or null if the drive doesn't have encoders.
	 */
	@Nullable
	private Double getRearRightMotorVelCached() {
		return rearRightMotor.getVelocity();
	}

	/**
	 * Get the cached position of the front right motor.
	 *
	 * @return The signed position in feet, or null if the drive doesn't have encoders.
	 */
	@Nullable
	private Double getFrontRightMotorPosCached() {
		return frontRightMotor.getPositionFeet();
	}

	/**
	 * Get the cached position of the front left motor.
	 *
	 * @return The signed position in feet, or null if the drive doesn't have encoders.
	 */
	@Nullable
	private Double getFrontLeftMotorPosCached() {
		return frontLeftMotor.getPositionFeet();
	}

	/**
	 * Get the cached position of the rear left motor.
	 *
	 * @return The signed position in feet, or null if the drive doesn't have encoders.
	 */
	@Nullable
	private Double getRearLeftMotorPosCached() {
		return rearLeftMotor.getPositionFeet();
	}

	/**
	 * Get the cached position of the rear right motor.
	 *
	 * @return The signed position in feet, or null if the drive doesn't have encoders.
	 */
	@Nullable
	private Double getRearRightMotorPosCached() {
		return rearRightMotor.getPositionFeet();
	}

}