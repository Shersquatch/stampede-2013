/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package edu.wpi.first.wpilibj.templates;

import edu.wpi.first.wpilibj.Counter;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Gyro;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.RobotDrive;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.Ultrasonic;

/**
 *
 * @author Robotics
 */
public class DriveTrain {

    RobotDrive drive;
    double gyroDrift;   // degrees per second;
    double gyroLastRdg;
    double gyroLastTime;
    boolean headingLock;    // heading lock mode
    double heading;         // the locked heading (according to gyro)
    double lastAngle;
    double lastAngleTime;
    Ultrasonic rightSonar;
    Ultrasonic leftSonar;
    boolean sonarLock, sonarLocked;
    Encoder frontLeftEncoder;
    Encoder frontRightEncoder;
    Encoder rearLeftEncoder;
    Encoder rearRightEncoder;
    Counter distanceCounter; 
    double sonarDistance;
    double sonarDifference;
    double y = 0;   // fwd speed
    double x = 0;   // side motion
    double r = 0;
    Gyro gyro;
    Joystick leftStick;
    Joystick rightStick;
    int approachButton,
            gyroLockButton;

    public void init() {
        drive = new RobotDrive(1, 2, 3, 4);
        gyro = new Gyro(1);


        distanceCounter = new Counter(2, 5); 
        distanceCounter.start(); 
        distanceCounter.reset(); 
        /* // Might be out of encoders... 
 
         frontLeftEncoder = new Encoder(2, 5, 2, 6);
         frontRightEncoder = new Encoder(2, 7, 2, 8); 
         rearLeftEncoder = new Encoder(2, 9, 2, 10);
         rearRightEncoder = new Encoder(2, 11, 2, 12); 
 
         frontLeftEncoder.start();
         frontRightEncoder.start();
         rearLeftEncoder.start();
         rearRightEncoder.start();
   
         */
        // note: 1 is output (marked INPUT on VEX!!!)
        // note: 2 is input (marked OUTPUT on VEX!!!)
        // note: 3 is output (marked INPUT on VEX!!!) 
        // note: 4 is input (marked OUTPUT on VEX!!!) 
        rightSonar = new Ultrasonic(1, 2);
        leftSonar = new Ultrasonic(3, 4);

        // start the gyro
        gyro.reset();
        gyro.setSensitivity(1.647 * 0.001);  // VEX gyro sensitivity (in mv/deg/sec)

        // start the sonar
        leftSonar.setAutomaticMode(true);
        rightSonar.setAutomaticMode(true);

        // prepare for sample collection for gyro drift correction
        gyroLastRdg = gyro.getAngle();
        gyroLastTime = Timer.getFPGATimestamp();
        gyroDrift = 0;

        // initiate the drive system
        drive.setInvertedMotor(RobotDrive.MotorType.kRearLeft, false);
        drive.setInvertedMotor(RobotDrive.MotorType.kRearRight, true);
        drive.setInvertedMotor(RobotDrive.MotorType.kFrontLeft, false);
        drive.setInvertedMotor(RobotDrive.MotorType.kFrontRight, true);
    }

    public void handler() {
        //handle sonar readings 
        sonarDistance = ((rightSonar.getRangeInches() + leftSonar.getRangeInches()) / 2);
        sonarDifference = rightSonar.getRangeInches() - leftSonar.getRangeInches();


    }

    public void ui() {

        x = leftStick.getX();
        y = leftStick.getY();
        r = rightStick.getX();

        double gyroAngle = gyro.getAngle();
        double time = Timer.getFPGATimestamp();

        if (false) {
            if (leftStick.getRawButton(gyroLockButton)) {
                // lock the heading
                if (!headingLock) {
                    headingLock = true;
                    heading = gyroAngle;
                }
            } else {
                headingLock = false;
            }

            if (!headingLock) {
                // standard control
                // joy1 - field drive, joy 2 - rotation speed
            } else {
                // heading-lock control
                // joy1 - field drive with fixed heading, joy2 - rotation speed (gyro-controlled)
                // adjust heading based on joy settings
                // scale to maximum degrees per second rate
                heading += 90 * r * (time - lastAngleTime);

                // adjust rotation setting to maintain heading
                double hdgError = gyroAngle - heading;
                if (Math.abs(hdgError) > 5) {
                    // set rotation
                    //...
                    r = Math.min(1, Math.abs(hdgError / 5)) * ((hdgError >= 0) ? 1 : -1);
                }
            }
            lastAngleTime = time;
            if (headingLock) {
                drive.mecanumDrive_Cartesian(x, y, r, gyroAngle);
            } else {
                drive.mecanumDrive_Cartesian(x, y, r, 0.0);
            }
        }

        if (false) {
            if (leftStick.getRawButton(approachButton) && sonarDistance < 30) {
                // stop moving forward
                if (y < 0) {
                    y = 0.0;
                }
                // wall alignment 
                if (sonarDifference > 1.5) {
                    r = -0.12;
                } else if (sonarDifference < -1.5) {
                    r = 0.12;
                } else {
                    r = 0.0;
                    // final approach 
                    if (sonarDistance > 12.0) {
                        y = -0.07;

                    } else {
                        y = 0.0;
                    }
                }
            }
        }
        drive.mecanumDrive_Cartesian(x, y, r, 0.0);
    }

    public void gyroInit() {
        gyro.reset();
        gyroLastRdg = gyro.getAngle();
        gyroLastTime = Timer.getFPGATimestamp();
        gyroDrift = 0;
    }

    public void gyroHandler() {
        // we should start reading the gyro here to correct for any drift
        // take a drift reading based on the last 5 seconds (MAY ADJUST!!!)
        double dt = Timer.getFPGATimestamp() - gyroLastTime;
        if (dt >= 10) {
            gyroLastTime = Timer.getFPGATimestamp();
        }
    }

    public void teleopInit() {
        gyro.reset();
    }
    
    public void drive(double x, double y, double r){
        drive.mecanumDrive_Cartesian(x, y, r, 0.0);
    }
}
