package minim.runtime

import java.awt.*
import java.awt.event.*
import java.awt.geom.AffineTransform
import java.awt.image.BufferStrategy
import java.awt.image.BufferedImage
import javax.imageio.ImageIO

class Window(width: Int, height: Int, title: String) : KeyListener, MouseListener, MouseMotionListener,
    MouseWheelListener {
    private val frame = Frame(title)
    private val canvas = Canvas()
    
    private val images = mutableListOf<BufferedImage>()
    
    private val buffer: BufferStrategy
    private val graphics: Graphics2D
    
    private val transformStack = mutableListOf<AffineTransform>()
    
    private val keys = Array(256) { Toggle() }
    private val buttons = Array(4) { Toggle() }
    
    private val pollBuffer = mutableListOf<Toggle>()
    
    val mousePoint = Point()
    
    init {
        val size = Dimension(width, height)
        
        canvas.minimumSize = size
        canvas.preferredSize = size
        canvas.maximumSize = size
        
        frame.add(canvas)
        frame.pack()
        frame.isResizable = false
        frame.setLocationRelativeTo(null)
        frame.addWindowListener(object : WindowAdapter() {
            override fun windowClosing(e: WindowEvent) {
                frame.isVisible = false
            }
        })
        
        val icon = ImageIO.read(javaClass.getResource("/img/monogram64.png"))
        frame.iconImage = icon
        
        canvas.createBufferStrategy(2)
        buffer = canvas.bufferStrategy
        graphics = buffer.drawGraphics as Graphics2D
        
        transformStack.add(0, graphics.transform)
        
        canvas.addKeyListener(this)
        canvas.addMouseListener(this)
        canvas.addMouseMotionListener(this)
        canvas.addMouseWheelListener(this)
    }
    
    fun open() {
        frame.isVisible = true
    }
    
    fun close() {
        frame.isVisible = false
        
        frame.dispose()
    }
    
    val isOpen get() = frame.isVisible
    
    fun flip() =
        buffer.show()
    
    fun getColor(): Color =
        graphics.color
    
    fun setColor(red: Int, green: Int, blue: Int, alpha: Int) {
        graphics.color = Color(red, green, blue, alpha)
    }
    
    fun getFont(): Font =
        graphics.font
    
    fun setFont(name: String, style: Int, size: Int) {
        graphics.font = Font(name, style, size)
    }
    
    fun getStroke() =
        graphics.stroke as BasicStroke
    
    fun setStroke(width: Float, cap: Int, join: Int, miterLimit: Float, dash: FloatArray, dashPhase: Float) {
        graphics.stroke = BasicStroke(width, cap, join, miterLimit, dash, dashPhase)
    }
    
    fun rotate(theta: Double) =
        graphics.rotate(theta)
    
    fun rotate(theta: Double, x: Double, y: Double) =
        graphics.rotate(theta, x, y)
    
    fun scale(x: Double, y: Double) =
        graphics.scale(x, y)
    
    fun shear(x: Double, y: Double) =
        graphics.shear(x, y)
    
    fun translate(x: Double, y: Double) =
        graphics.translate(x, y)
    
    fun pushState() {
        transformStack.add(0, graphics.transform)
    }
    
    fun popState() {
        transformStack.removeAt(0)
        
        if (transformStack.isEmpty()) {
            transformStack.add(0, AffineTransform())
        }
        
        graphics.transform = transformStack.first()
    }
    
    fun clear() =
        graphics.fillRect(0, 0, canvas.width, canvas.height)
    
    fun drawLine(xa: Int, ya: Int, xb: Int, yb: Int) =
        graphics.drawLine(xa, ya, xb, yb)
    
    fun drawRect(x: Int, y: Int, width: Int, height: Int) =
        graphics.drawRect(x, y, width, height)
    
    fun fillRect(x: Int, y: Int, width: Int, height: Int) =
        graphics.fillRect(x, y, width, height)
    
    fun drawOval(x: Int, y: Int, width: Int, height: Int) =
        graphics.drawOval(x, y, width, height)
    
    fun fillOval(x: Int, y: Int, width: Int, height: Int) =
        graphics.fillOval(x, y, width, height)
    
    fun drawRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int) =
        graphics.drawRoundRect(x, y, width, height, arcWidth, arcHeight)
    
    fun fillRoundRect(x: Int, y: Int, width: Int, height: Int, arcWidth: Int, arcHeight: Int) =
        graphics.fillRoundRect(x, y, width, height, arcWidth, arcHeight)
    
    fun draw3DRect(x: Int, y: Int, width: Int, height: Int, raised: Boolean) =
        graphics.draw3DRect(x, y, width, height, raised)
    
    fun fill3DRect(x: Int, y: Int, width: Int, height: Int, raised: Boolean) =
        graphics.fill3DRect(x, y, width, height, raised)
    
    fun drawArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int) =
        graphics.drawArc(x, y, width, height, startAngle, arcAngle)
    
    fun fillArc(x: Int, y: Int, width: Int, height: Int, startAngle: Int, arcAngle: Int) =
        graphics.fillArc(x, y, width, height, startAngle, arcAngle)
    
    fun drawPolyline(xPoints: IntArray, yPoints: IntArray, nPoints: Int) =
        graphics.drawPolyline(xPoints, yPoints, nPoints)
    
    fun drawPolygon(xPoints: IntArray, yPoints: IntArray, nPoints: Int) =
        graphics.drawPolygon(xPoints, yPoints, nPoints)
    
    fun fillPolygon(xPoints: IntArray, yPoints: IntArray, nPoints: Int) =
        graphics.fillPolygon(xPoints, yPoints, nPoints)
    
    fun drawString(str: String, x: Int, y: Int) =
        graphics.drawString(str, x, y)
    
    fun loadImage(path: String): Triple<Int, Int, Int> {
        val image = ImageIO.read(javaClass.getResource(path))
        
        images += image
        
        return Triple(images.lastIndex, image.width, image.height)
    }
    
    fun drawImage(image: Int, x: Int, y: Int) =
        graphics.drawImage(images[image], x, y, null)
    
    fun drawImage(image: Int, x: Int, y: Int, width: Int, height: Int) =
        graphics.drawImage(images[image], x, y, width, height, null)
    
    fun drawImage(image: Int, dxa: Int, dya: Int, dxb: Int, dyb: Int, sxa: Int, sya: Int, sxb: Int, syb: Int) =
        graphics.drawImage(images[image], dxa, dya, dxb, dyb, sxa, sya, sxb, syb, null)
    
    fun keyIsDown(keyCode: Int): Boolean {
        if (keyCode in keys.indices) {
            return keys[keyCode].isDown
        }
        
        return false
    }
    
    fun keyIsHeld(keyCode: Int): Boolean {
        if (keyCode in keys.indices) {
            return keys[keyCode].isHeld
        }
        
        return false
    }
    
    fun keyIsUp(keyCode: Int): Boolean {
        if (keyCode in keys.indices) {
            return keys[keyCode].isUp
        }
        
        return false
    }
    
    fun buttonIsDown(buttonCode: Int): Boolean {
        if (buttonCode in buttons.indices) {
            return buttons[buttonCode].isDown
        }
        
        return false
    }
    
    fun buttonIsHeld(buttonCode: Int): Boolean {
        if (buttonCode in buttons.indices) {
            return buttons[buttonCode].isHeld
        }
        
        return false
    }
    
    fun buttonIsUp(buttonCode: Int): Boolean {
        if (buttonCode in buttons.indices) {
            return buttons[buttonCode].isUp
        }
        
        return false
    }
    
    class Toggle {
        private var now = false
        private var then = false
        
        val isDown get() = now && !then
        
        val isHeld get() = now
        
        val isUp get() = !now && then
        
        fun set(on: Boolean) {
            now = on
        }
        
        fun poll() {
            then = now
        }
    }
    
    fun poll() {
        pollBuffer.forEach(Toggle::poll)
        
        pollBuffer.clear()
    }
    
    override fun keyTyped(e: KeyEvent) {
        e.consume()
    }
    
    override fun keyPressed(e: KeyEvent) {
        if (e.keyCode in keys.indices) {
            val key = keys[e.keyCode]
            
            key.set(true)
            
            pollBuffer.add(key)
        }
    }
    
    override fun keyReleased(e: KeyEvent) {
        if (e.keyCode in keys.indices) {
            val key = keys[e.keyCode]
            
            key.set(false)
            
            pollBuffer.add(key)
        }
    }
    
    override fun mouseClicked(e: MouseEvent) {
        e.consume()
    }
    
    override fun mousePressed(e: MouseEvent) {
        if (e.button in buttons.indices) {
            val button = buttons[e.button]
            
            button.set(true)
            
            pollBuffer.add(button)
        }
    }
    
    override fun mouseReleased(e: MouseEvent) {
        if (e.button in buttons.indices) {
            val button = buttons[e.button]
            
            button.set(true)
            
            pollBuffer.add(button)
        }
    }
    
    override fun mouseEntered(e: MouseEvent) {
    }
    
    override fun mouseExited(e: MouseEvent) {
    }
    
    override fun mouseDragged(e: MouseEvent) {
        mousePoint.location = e.point
    }
    
    override fun mouseMoved(e: MouseEvent) {
        mousePoint.location = e.point
    }
    
    override fun mouseWheelMoved(e: MouseWheelEvent) {
    }
}