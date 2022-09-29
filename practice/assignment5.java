// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import com.revrobotics.CANSparkMax;
import com.revrobotics.CANSparkMaxLowLevel;
import com.revrobotics.CANSparkMaxLowLevel.MotorType;
import com.ctre.phoenix.motorcontrol.can.WPI_VictorSPX;
// import com.ctre.phoenix.motorcontrol.can.*;

//to access camera used on robot connected to drivers station
import edu.wpi.first.cameraserver.CameraServer;
//imports to implement CAN motors
import edu.wpi.first.wpilibj.CAN;
//import to create a joystick to control robot
import edu.wpi.first.wpilibj.Joystick;
//adds periodic() functions that are called on interval
import edu.wpi.first.wpilibj.TimedRobot;
//import to configure skid-steering with two or more wheels
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
//imports basic functions to control motors such as set() and stopMotor()
import edu.wpi.first.wpilibj.motorcontrol.MotorControllerGroup;
//presents a selection of options on the smart dash
//allows you to switch between multiple auto's
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
//a interface to interconnect the robot program and drive station
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;
//A simple timer import to use basic timers
import edu.wpi.first.wpilibj.Timer;
//an import for accessing a usb camera
import edu.wpi.first.cscore.UsbCamera;

/**
 * The VM is configured to automatically run this class, and to call the
 * functions corresponding to
 * each mode, as described in the TimedRobot documentation. If you change the
 * name of this class or
 * the package after creating this project, you must also update the
 * build.gradle file in the
 * project.
 */
public class Robot extends TimedRobot {
  //constant strings to switch between custom autos using switch statmentin autonomousPeriodic()
  private static final String kDefaultAuto = "Default";
  private static final String kCustomAuto = "Custom Auto";
  private static final String kCustomAuto2 = "Custom Auto 2";
  //string variable to change between autos
  private String m_autoSelected;
  //used to integrate with SmartDash
  private final SendableChooser<String> m_chooser = new SendableChooser<>();
  
  //CAN Pin Constants
  //Variable indexs for the pin ID's of 
  private final int m_storageDevice = 1;
  private final int m_shooterDevice = 2;
  private final int m_intakeDevice = 3;
  private final int m_leftleadDevice = 4;
  private final int m_rightleadDevice = 5;
  private final int m_leftbackDevice = 6;
  private final int m_rightbackDevice = 11;
  private final int m_hanger3 = 10;
  private final int m_hanger4 = 12;
  // private final int m_hanger5 = 12;
  // private final int m_hanger6 = 13;

  //Joystick
  //creating the joystick objects and setting ID's
  private final Joystick m_xbox = new Joystick(0);//MAKE SURE IN DRIVERSTATION CONTROLLER IS ON 0.
  private final Joystick m_stick = new Joystick(1);//MAKE SURE IN DRIVERSTATION CONTROLLER IS ON 1.
  private final Joystick m_test = new Joystick(2);//MAKE SURE IN DRIVERSTATION CONTROLLER IS ON 2.
  private final Joystick m_test2 = new Joystick(3);//MAKE SURE IN DRIVERSTATION CONTROLLER IS ON 2.


  //Hanging
  //victorSPX motors to control hangar
  private final WPI_VictorSPX OuterLeftClimber = new WPI_VictorSPX(m_hanger3);
  private final WPI_VictorSPX OuterRightClimber = new WPI_VictorSPX(m_hanger4);
  // private final WPI_VictorSPX InnerClimberLateral = new WPI_VictorSPX(m_hanger5);
  // private final WPI_VictorSPX OuterClimberLateral = new WPI_VictorSPX(m_hanger6);
  
  //DifferentialDrive
  //4 motor controllers
  private final WPI_VictorSPX frontLeft = new WPI_VictorSPX(m_leftleadDevice);
  private final WPI_VictorSPX frontRight = new WPI_VictorSPX(m_rightleadDevice);
  private final WPI_VictorSPX backRight = new WPI_VictorSPX(m_rightbackDevice);
  private final WPI_VictorSPX backLeft = new WPI_VictorSPX(m_leftbackDevice);
  //two motor controller groups to group the four motors into left and right
  private final MotorControllerGroup m_left = new MotorControllerGroup(frontLeft, frontRight);
  private final MotorControllerGroup m_right = new MotorControllerGroup(backLeft, backRight);
  //groupping the two motorcontrollergroups together
  private final DifferentialDrive m_robotDrive = new DifferentialDrive(m_left, m_right);

  //Intake
  //canSparkMax motor controller
  private final CANSparkMax m_intake = new CANSparkMax(m_intakeDevice, MotorType.kBrushless);

  //Storage/Shooter
  //canSparkMax motor controller for shooters and storage
  private final CANSparkMax m_shooter = new CANSparkMax(m_shooterDevice, MotorType.kBrushless);
  private final CANSparkMax m_storage = new CANSparkMax(m_storageDevice, MotorType.kBrushless);
  
  // Camera, defined as global variable, change later if necessary
  // private UsbCamera camera = null;

  // Autonomous Variables
  //new timer object
  private final Timer timer = new Timer();
  //phase to control what step of auto robot is on
  private int phase = 0;
  
  /**
   * This function is run when the robot is first started up and should be used
   * for any
   * initialization code.
   */
  @Override
  //robot init code
  public void robotInit() {
    // Reset Shooter and Storage motors
    m_shooter.restoreFactoryDefaults();
    m_storage.restoreFactoryDefaults();

    //setting left motorcontrollergroup to inverted
    m_left.setInverted(true);

    // Camera
    // camera = CameraServer.startAutomaticCapture();
    // camera.setResolution(320, 240);

    // Default
    m_chooser.setDefaultOption("Default Auto", kDefaultAuto);
    m_chooser.addOption("Custom Auto", kCustomAuto);
    m_chooser.addOption("Custom Auto 2", kCustomAuto2);
    SmartDashboard.putData("Auto choices", m_chooser);
  }

  /**
   * This function is called every robot packet, no matter the mode. Use this for
   * items like
   * diagnostics that you want ran during disabled, autonomous, teleoperated and
   * test.
   *
   * <p>
   * This runs after the mode specific periodic functions, but before LiveWindow
   * and
   * SmartDashboard integrated updating.
   */
  @Override
  public void robotPeriodic() {
  }

  /**
   * This autonomous (along with the chooser code above) shows how to select
   * between different
   * autonomous modes using the dashboard. The sendable chooser code works with
   * the Java
   * SmartDashboard. If you prefer the LabVIEW Dashboard, remove all of the
   * chooser code and
   * uncomment the getString line to get the auto name from the text box below the
   * Gyro
   *
   * <p>
   * You can add additional auto modes by adding additional comparisons to the
   * switch structure
   * below with additional strings. If using the SendableChooser make sure to add
   * them to the
   * chooser code above as well.
   */
  @Override
  //function to init auto and chooser the auto route
  public void autonomousInit() {
    m_autoSelected = m_chooser.getSelected();
    // m_autoSelected = SmartDashboard.getString("Auto Selector", kDefaultAuto);
    System.out.println("Auto selected: " + m_autoSelected);

    //reset and starting timer
    timer.reset();
    timer.start();
  }

  //NOTE - Each phase has the time relative to phase

  /** This function is called periodically during autonomous. */
  @Override
  public void autonomousPeriodic() {
    switch (m_autoSelected) {
      case kCustomAuto2:
        if (phase == 0) {
          //for first 3.8 seconds:
          if (timer.get() < 3.8) {
            //for first 1 second drive at 0.55
            if (timer.get() < 1.0) {
              m_robotDrive.arcadeDrive(0.55, 0.0);
            }
            //for next 1.5 seconds, stop
            else if (timer.get() < 2.5){
              m_robotDrive.arcadeDrive(0.0, 0.0);
            }
            //for rest of time, drive at 0.55
            else {
              m_robotDrive.arcadeDrive(0.55, 0.0);
            }
          } else {
            //increment phase
            phase++;
            timer.reset();
          }
        }
        if (phase == 1) {
          //for first second, use shooter
          if (timer.get() < 1.0) {
            m_shooter.set(0.575);
          } else {
            //increment phase
            phase++;
            timer.reset();
          }
        }
        if (phase == 2) {
          //for first 1.3s drive back at -0.55
          if (timer.get() < 1.3) {
            m_robotDrive.arcadeDrive(-0.55, 0.0);
          } else {
            //increment phase
            phase++;
            timer.reset();
            //stop motor
            m_robotDrive.arcadeDrive(0.0, 0.0);
          }
        }
        if (phase == 3) {
          //while timer less than three:
          if (timer.get() < 3.0) {
            //wait for one second then enable storage at -0.95
            if (timer.get() > 1.0) {
              m_storage.set(-0.95);
            }
          } else {
            //increment and stop all motors
            phase++;
            m_shooter.set(0.0);
            m_storage.set(0.0);
            timer.reset();
            m_robotDrive.arcadeDrive(0.0, 0.6);

          }
        }
        break;
      case kCustomAuto:
        if (phase == 0) {
          //while timer < 2.3:
          if (timer.get() < 2.3) {
            //for 0.8s start shooter and drive forward
            if (timer.get() < 0.8) {
              m_shooter.set(0.575);
              m_robotDrive.arcadeDrive(0.55, 0.0);
            }
            //for next 1s set storage to -0.95
            else if (timer.get() < 1.8){
              m_robotDrive.arcadeDrive(0.0, 0.0);
              m_storage.set(-0.95);
            }
            else {
              //go forward with intake for last time period
              m_robotDrive.arcadeDrive(0.55, 0.0);
              m_intake.set(0.90);
              m_storage.stopMotor();
            }
          } else {
            //increment
            phase = 1;
            timer.reset();
          }
        }
        if (phase == 1) {
          //for 1.4s enable shooter
          if (timer.get() < 1.4) {
            m_shooter.set(0.575);
          } else {
            //increment
            phase = 2;
            timer.reset();
          }
        }
        if (phase == 2) {
          //for 0.25s drive backwards
          if (timer.get() < 0.25) {
            m_robotDrive.arcadeDrive(-0.55, 0.0);
            
          }
          else {
            //increment and off shooter
            phase = 3;
            timer.reset();
            m_intake.set(0.0);
            m_robotDrive.arcadeDrive(0, 0);
          }
        }
        if (phase == 3) {
          //while timer < 3.0:
          if (timer.get() < 3.0) {
            //wait for one second then start storage motor
            if (timer.get() > 1.0) {
              m_storage.set(-0.95);
            }
          }
          else {
            //stop motors and increment
            m_storage.stopMotor();
            m_shooter.stopMotor();
            phase = 4 ;
            //turn
            m_robotDrive.arcadeDrive(0.0, 0.6);

          }
        }
        break;
      case kDefaultAuto:
      default:
        if (phase == 0) {
          //while less than 3.8s
          if (timer.get() < 3.8) {
            //for 1s enable shooter and arcade drive
            if (timer.get() < 1.0) {
              m_shooter.set(0.575);
              m_robotDrive.arcadeDrive(0.55, 0.0);
            }
            //for 1.5s set shooter storage to -0.95
            else if (timer.get() < 2.5){
              m_robotDrive.arcadeDrive(0.0, 0.0);
              m_storage.set(-0.95);
            }
            else {
              //for rest of time, do arcade drive and intake
              m_robotDrive.arcadeDrive(0.55, 0.0);
              m_intake.set(0.90);
              m_storage.stopMotor();
            }
          }
          else {
            //increment
            phase++;
            timer.reset();
          }
        }
        if (phase == 1) {
          //for 1s, enable shooter
          if (timer.get() < 1.0) {
            m_shooter.set(0.575);
          } else {
            //increment
            phase++;
            timer.reset();
          }
        }
        if (phase == 2) {
          //for 1.3s, arcade drive enable
          if (timer.get() < 1.3) {
            m_robotDrive.arcadeDrive(-0.55, 0.0);
          }
        else {
          //increment and stop motors
          phase = 3;
          timer.reset();
          m_intake.set(0.0);
          m_robotDrive.arcadeDrive(0.0, 0.0);
         }
      }
      if (phase == 3) {
        //for 3s
        if (timer.get() < 3.0) {
          //wait 1s then set storage motor
         if (timer.get() > 1.0) {
            m_storage.set(-0.95);
          }
       }
       else {
        //increment and stop motors
         phase=4;
         m_shooter.set(0.0);
         m_storage.set(0.0);
         timer.reset();
        }
      }

      if (phase == 4) {
        //turn
        m_robotDrive.arcadeDrive(0.0, 0.6);
      }
      break;
    }
  }

  /** This function is called once when teleop is enabled. */
  @Override
  //stop robot on tele init
  public void teleopInit() {
    m_robotDrive.arcadeDrive(0.0, 0.0);
  }

  /** This function is called periodically during operator control. */
  @Override
  public void teleopPeriodic() {
    //call these functions periodically to check for input
    Intake();
    Hanging1();
    Shooting();
    Movement();
    Storage();

  }
  
  public void Shooting() {
    //if xbox button is pressed, control shooter
  if (m_xbox.getRawButton(5))
  {
       m_shooter.set(0.585);
  }
  if (m_xbox.getRawButton(7))
  {
       m_shooter.set(0.50);
  
  }if (m_xbox.getRawButton(4))
  {
       m_shooter.set(0.0);
  }
}

  public void Movement() {
  //configuring joystick
    double xDrive = -(m_stick.getRawAxis(4));
    double yDrive = -(m_stick.getRawAxis(1));
    //calling arcade for analag stick input
    m_robotDrive.arcadeDrive(yDrive, xDrive);

    //movement with xbox joystick
    if (m_test2.getRawButton(1)) {
      frontLeft.set(0.80);
    }

    if (m_test2.getRawButton(2)) {
      frontRight.set(0.80);
    }

    if (m_test2.getRawButton(3)) {
      backLeft.set(0.80);
    }

    if (m_test2.getRawButton(4)) {
      backRight.set(0.80);
    }

    //stop all motors on button 5 press
    if (m_test2.getRawButton(5)) {
      frontLeft.stopMotor();
      frontRight.stopMotor();
      backLeft.stopMotor();
      backRight.stopMotor();
    }
  
  }
  
  public void Storage() {
  
    //perform various storage motor controls using xbox controller
    if (m_xbox.getRawButton(6)) {
      m_storage.set(-0.95); 
    } 
  
    if (m_xbox.getRawButton(8)) { 
      m_storage.set(-0.65); 
    }
    
    if (m_xbox.getRawButton(2)) {
      m_storage.set(0.0);
    }
  
  }
  


  //Intake function to be called in teleop
  public void Intake() {

    //perform various intake controls using xbox
    if (m_xbox.getRawButtonPressed(1)) {
        m_intake.set(0.80);   
      }
      
    if (m_xbox.getRawButtonPressed(3)) {
        m_intake.set(0.0);   
      }
      
    if (m_xbox.getRawButtonPressed(9)) {
        m_intake.set(-0.95);   
      }
    
  }

  public void Hanging1()
  {
    //config of climber motors
    OuterLeftClimber.set(m_xbox.getRawAxis(1));
    OuterRightClimber.set(m_xbox.getRawAxis(1));

    //if xbox buttons are pressed, perform features on climber motors
    if (m_test.getRawButton(4)) {
      OuterLeftClimber.set(0.90);
    }

    if (m_test.getRawButton(2)) {
      OuterLeftClimber.set(-0.90);
    }

    if (m_test.getRawButton(5)) {
      OuterLeftClimber.set(0.0);
    }

    if (m_test.getRawButton(3)) {
      OuterRightClimber.set(0.90);
    }

    if (m_test.getRawButton(1)) {
      OuterRightClimber.set(-0.90);
    }

    if (m_xbox.getRawButton(10)) 
    {
      OuterLeftClimber.set(0.0);
      OuterRightClimber.set(0.0);
    }
    
    if (m_xbox.getRawButton(11)) 
    {
      OuterLeftClimber.set(1.0);
      OuterRightClimber.set(1.0);
    }
    
    if (m_xbox.getRawButton(12)) 
    {
      OuterLeftClimber.set(-1.0);
      OuterRightClimber.set(-1.0);
    }
  }
  
  /** This function is called once when the robot is disabled. */
  @Override
  public void disabledInit() {
  }

  /** This function is called periodically when disabled. */
  @Override
  public void disabledPeriodic() {
  }

  /** This function is called once when test mode is enabled. */
  @Override
  public void testInit() {
  }

  /** This function is called periodically during test mode. */
  @Override
  public void testPeriodic() {
  }
  
}
