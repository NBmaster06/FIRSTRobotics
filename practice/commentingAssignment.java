// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

//following code in placed in the frc.robot package
package frc.robot;

//imports
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;

public class Robot extends TimedRobot {

  //variable declaration and initialization
  private final PWMSparkMax m_leftDrive = new PWMSparkMax(0);
  private final PWMSparkMax m_rightDrive = new PWMSparkMax(1);
  private final DifferentialDrive m_robotDrive = new DifferentialDrive(m_leftDrive, m_rightDrive);
  private final Joystick m_stick = new Joystick(0);
  private final Timer m_timer = new Timer();
  private int count = 0;

  //robotInit is called when the program starts
  @Override
  public void robotInit() {
    //sets m_rightDrive's state of inversion to true
    m_rightDrive.setInverted(true);
  }

  //autonomousInit is called everytime the robot switches to autonomous mode
  @Override
  public void autonomousInit() {
    //timer is reset
    m_timer.reset();
    //timer is started
    m_timer.start();
  }

  //autonomousPeriodic is run every 20ms(default) during autonomous mode
  @Override
  public void autonomousPeriodic() {
    //robot drives for 2 seconds at half speed using the arcadeDrive
    //robot stops after 2 seconds and count is incremented
    if (count==0){
      if (m_timer.get() < 2.0) {
        m_robotDrive.arcadeDrive(0.5, 0.0); 
      } else {
        m_robotDrive.stopMotor(); 
        count++;
      }
      //robot drives backward at half speed for 4 seconds using the arcadeDrive
      //robot stops after 4 seconds and count is incremented
    } else if (count==1){
      if (m_timer.get() < 4.0) {
        m_robotDrive.arcadeDrive(-0.5, 0.0); 
      } else {
        m_robotDrive.stopMotor(); 
        count++;
      }
      //robot turns around the z-axis clockwise at a rotation rate of 0.5 for 6 seconds
      //robot stops and count is incremented
    } else if (count==2){
      if (m_timer.get() < 6.0) {
        m_robotDrive.arcadeDrive(0.0, 0.5); 
      } else {
        m_robotDrive.stopMotor(); 
        count++;
      }
    }
  }

  //teleopInit is called when teleoperation mode is started
  @Override
  public void teleopInit() {}

  //teleopPeriodic is called every 20ms(default) during teleoperation mode
  @Override
  public void teleopPeriodic() {
    //Maps the y-axis of the joystick to forward and back and the x-axis to turning
    m_robotDrive.arcadeDrive(m_stick.getY(), m_stick.getX());
  }

  //testing mode init and periodic(20ms default)
  @Override
  public void testInit() {}

  @Override
  public void testPeriodic() {}
}
