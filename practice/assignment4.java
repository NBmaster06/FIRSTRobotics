// Copyright (c) FIRST and other WPILib contributors.
// Open Source Software; you can modify and/or share it under the terms of
// the WPILib BSD license file in the root directory of this project.

package frc.robot;

import edu.wpi.first.wpilibj.AnalogGyro;
import edu.wpi.first.wpilibj.Encoder;
import edu.wpi.first.wpilibj.Joystick;
import edu.wpi.first.wpilibj.TimedRobot;
import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.drive.DifferentialDrive;
import edu.wpi.first.wpilibj.motorcontrol.PWMSparkMax;

public class Robot extends TimedRobot {

  private final PWMSparkMax m_leftDrive = new PWMSparkMax(0);
  private final PWMSparkMax m_rightDrive = new PWMSparkMax(1);
  private final PWMSparkMax m_shooter = new PWMSparkMax(2);
  private final PWMSparkMax m_intake = new PWMSparkMax(3);
  private final DifferentialDrive m_robotDrive = new DifferentialDrive(m_leftDrive, m_rightDrive);
  private final AnalogGyro gyro = new AnalogGyro(0);
  private final Encoder encoder = new Encoder(0, 1);
  private final Joystick m_stick = new Joystick(0);
  private final Timer m_timer = new Timer();
  private int count = 0;

  @Override
  public void robotInit() {
    m_rightDrive.setInverted(true);
  }

  @Override
  public void autonomousInit() {
    m_timer.reset();
    m_timer.start();
    encoder.reset();
    gyro.reset();
  }

  @Override
  public void autonomousPeriodic() 
  {
    //driving 5 feet forward to pick up blue ball #1 while intake is running
    if(count == 0)
    {
      if(encoder.getDistance() < 5.0)
      {
        m_robotDrive.arcadeDrive(0.5, 0.0);
        m_intake.set(0.5);
      }
      else
      {
        m_intake.stopMotor();
        count++;
      }
    }
    //turning 90 deg clockwise using the AnalogGyro class
    else if(count == 1)
    {
      if(gyro.getAngle() < 90.0)
      {
        m_robotDrive.arcadeDrive(0.0, 0.5);
      }
      else 
      {
        encoder.reset();
        count++;
      }
    }
    //driving 10 feet forward to pick up blue ball #2 while intake is running
    else if(count == 2)
    {
      if(encoder.getDistance() < 10.0)
      {
        m_robotDrive.arcadeDrive(0.5, 0.0);
        m_intake.set(0.5);
      }
      else
      {
        m_robotDrive.stopMotor();
        m_intake.stopMotor();
        count++;
      }
    }
    //after all sections are complete, start the shooter for 5s to shoot blue ball #1 and #2 into the goal
    else
    {
      m_timer.reset();
      m_timer.start();
      if(m_timer.get() < 5.0)
      {
        m_shooter.set(0.5);
      }
      else
      {
        m_shooter.stopMotor();
      }
    }
  }

  @Override
  public void teleopInit() {}

  @Override
  public void teleopPeriodic() 
  {
    //when button 0 is pressed, intake turns off
    if(m_stick.getRawButtonPressed(0))
    {
      m_intake.stopMotor();
    }
    //when button 1 is pressed, intake turns on
    else if(m_stick.getRawButtonPressed(1))
    {
      m_intake.set(0.5);
    }
    //when button 2 is pressed, intake runs in reverse
    else if(m_stick.getRawButtonPressed(2))
    {
      m_intake.set(-0.5);
    }
  }

  @Override
  public void testInit() {}

  @Override
  public void testPeriodic() {}
}
