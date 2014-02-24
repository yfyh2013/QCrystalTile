package com.Softwareprojekt.visualization;

import com.Softwareprojekt.Utilities.ExtendedPickingSupport;
import org.jzy3d.chart.Chart;
import org.jzy3d.chart.controllers.camera.AbstractCameraController;
import org.jzy3d.chart.controllers.mouse.AWTMouseUtilities;
import org.jzy3d.chart.controllers.thread.camera.CameraThreadController;
import org.jzy3d.maths.BoundingBox3d;
import org.jzy3d.maths.Coord2d;
import org.jzy3d.maths.IntegerCoord2d;
import org.jzy3d.plot3d.rendering.scene.Graph;
import org.jzy3d.plot3d.rendering.view.modes.ViewPositionMode;

import javax.media.opengl.GL;
import javax.media.opengl.glu.GLU;
import javax.swing.*;
import java.awt.event.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SpaceGroupViewChartController extends AbstractCameraController implements
        ActionListener, MouseListener, MouseWheelListener, MouseMotionListener {

    protected final ExtendedPickingSupport _pickingSupport;
    protected final Chart _chart;
    protected final GLU glu = new GLU();
    protected final JPopupMenu _contextMenu;
    protected  final Map<JMenuItem, ViewPositionMode> _modes = new HashMap<JMenuItem, ViewPositionMode>();

    protected final ResourceBundle bundle = ResourceBundle.getBundle("Messages");

    private static final float Min_Bounding_Box_Value = -10f;
    private static final float Max_Bounding_Box_Value = 1f;

    public SpaceGroupViewChartController(Chart chart) {
        register(chart);
        addSlaveThreadController(new CameraThreadController(chart));

        this._chart = chart;
        this._chart.addKeyController();
        this._chart.addScreenshotKeyController();
        this._pickingSupport = new ExtendedPickingSupport();

        this._contextMenu = new JPopupMenu();
        this._contextMenu.setLightWeightPopupEnabled(false);
        // create view menu items
        final JMenu mnuView = new JMenu(bundle.getString("camera"));
        final JMenuItem miViewFree = new JMenuItem(bundle.getString("cameraFree"));
        this._modes.put(miViewFree, ViewPositionMode.FREE);
        final JMenuItem miViewTop = new JMenuItem(bundle.getString("cameraTop"));
        this._modes.put(miViewTop, ViewPositionMode.TOP);
        final JMenuItem miViewProfile = new JMenuItem(bundle.getString("cameraProfile"));
        this._modes.put(miViewProfile, ViewPositionMode.PROFILE);

        miViewFree.addActionListener(this);
        miViewTop.addActionListener(this);
        miViewProfile.addActionListener(this);

        mnuView.add(miViewFree);
        mnuView.add(miViewTop);
        mnuView.add(miViewProfile);
        this._contextMenu.add(mnuView);
    }

    public void register(Chart chart) {
        super.register(chart);
        chart.getCanvas().addMouseController(this);
    }

    public void dispose() {
        for (Chart chart : targets) {
            chart.getCanvas().removeMouseController(this);
        }
        super.dispose();
    }

    public boolean handleSlaveThread(MouseEvent e) {
        if (AWTMouseUtilities.isDoubleClick(e)) {
            if (threadController != null) {
                threadController.start();
                return true;
            }
        }
        if (threadController != null)
            threadController.stop();
        return false;
    }

    public JPopupMenu getPopupMenu() {
        return this._contextMenu;
    }

    public ExtendedPickingSupport getPickingSupport() {
        return this._pickingSupport;
    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent e) {
        float factor = (e.getWheelRotation() / 10.0f);
        final BoundingBox3d bound = this._chart.getView().getBounds();

        bound.setXmin(Math.max(Math.min(bound.getXmin() - factor, Max_Bounding_Box_Value), Min_Bounding_Box_Value));
        bound.setYmin(Math.max(Math.min(bound.getYmin() - factor, Max_Bounding_Box_Value), Min_Bounding_Box_Value));
        bound.setZmin(Math.max(Math.min(bound.getZmin() - factor, Max_Bounding_Box_Value), Min_Bounding_Box_Value));
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            int yflip = -e.getY() + this._chart.getCanvas().getRendererHeight();
            Graph graph = _chart.getScene().getGraph();
            GL gl = _chart.getView().getCurrentGL();

            // will trigger vertex selection event to those subscribing to PickingSupport.
            this._pickingSupport.pickObjects(gl, glu, this._chart.getView(), graph, new IntegerCoord2d(e.getX(), yflip));
            // release gl context
            this._chart.getView().getCurrentContext().release();
        }
        else if (SwingUtilities.isRightMouseButton(e)) {
            this._contextMenu.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
        //
        if (handleSlaveThread(e))
            return;

        prevMouse.x = e.getX();
        prevMouse.y = e.getY();
    }

    @Override
    public void mouseReleased(MouseEvent e) { }

    @Override
    public void mouseEntered(MouseEvent e) { }

    @Override
    public void mouseExited(MouseEvent e) { }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() instanceof JMenuItem) {
            final JMenuItem item = (JMenuItem)e.getSource();
            if (this._modes.containsKey(item)) {
                _chart.getView().setViewPositionMode(this._modes.get(item));
            }
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Coord2d mouse = new Coord2d(e.getX(), e.getY());

        // Rotate
        if (AWTMouseUtilities.isLeftDown(e)) {
            Coord2d move = mouse.sub(prevMouse).div(100);
            rotate(move);
        }
        this.prevMouse = mouse;
    }

    @Override
    public void mouseMoved(MouseEvent e) { }
}
